package io.github.prjkmo112.cafeapi.concurrency;

import io.github.prjkmo112.cafeapi.common.dto.event.OrderPaidEvent;
import io.github.prjkmo112.cafeapi.common.exception.BusinessException;
import io.github.prjkmo112.cafeapi.common.exception.ErrorCode;
import io.github.prjkmo112.cafeapi.domain.menu.entity.Menu;
import io.github.prjkmo112.cafeapi.domain.menu.entity.MenuStatus;
import io.github.prjkmo112.cafeapi.domain.menu.repository.MenuRepository;
import io.github.prjkmo112.cafeapi.domain.order.dto.CreateOrderRequestDto;
import io.github.prjkmo112.cafeapi.domain.order.facade.OrderFacade;
import io.github.prjkmo112.cafeapi.domain.order.repository.OrderRepository;
import io.github.prjkmo112.cafeapi.domain.point.dto.PointChargeRequestDto;
import io.github.prjkmo112.cafeapi.domain.point.entity.UserPoint;
import io.github.prjkmo112.cafeapi.domain.point.repository.PointHistoryRepository;
import io.github.prjkmo112.cafeapi.domain.point.repository.UserPointRepository;
import io.github.prjkmo112.cafeapi.domain.point.service.PointService;
import io.github.prjkmo112.cafeapi.domain.user.entity.User;
import io.github.prjkmo112.cafeapi.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 MySQL(Testcontainers)에 동시 요청을 쏴서 락 / 유니크 제약이 정합성을 지키는지 검증한다.
 * Mockito 단위 테스트로는 검증할 수 없는 영역(비관적 락, 멱등키 유니크 제약)을 다룬다.
 * 실행하려면 Docker 가 떠 있어야 한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class ConcurrencyIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

    // Kafka 발행은 이 테스트의 관심사가 아니므로 막아둠
    @MockitoBean
    KafkaTemplate<String, OrderPaidEvent> kafkaTemplate;

    @Autowired OrderFacade orderFacade;
    @Autowired PointService pointService;
    @Autowired UserRepository userRepository;
    @Autowired MenuRepository menuRepository;
    @Autowired UserPointRepository userPointRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PointHistoryRepository pointHistoryRepository;

    private User user;
    private Menu menu;

    @BeforeEach
    void setUp() {
        pointHistoryRepository.deleteAll();
        orderRepository.deleteAll();
        userPointRepository.deleteAll();
        userRepository.deleteAll();
        menuRepository.deleteAll();

        user = userRepository.save(User.builder()
                .name("동시성").email("concurrency@test.com").password("pw").build());
        userPointRepository.save(UserPoint.builder().user(user).build());
        menu = menuRepository.save(Menu.builder()
                .name("아메리카노").price(4_000L).status(MenuStatus.SALE).build());
    }

    @Test
    @DisplayName("잔액이 1건분일 때 동시에 10번 주문하면 1건만 성공하고 잔액은 0이다")
    void concurrentOrders_neverOverspendBalance() throws Exception {
        // given: 잔액 4000P (메뉴 1잔분)
        pointService.charge(new PointChargeRequestDto(user.getId(), 4_000L, "init"));

        // when: 서로 다른 멱등키로 10개 동시 주문
        List<Result> results = runConcurrently(10, i ->
                orderFacade.createOrder(new CreateOrderRequestDto(user.getId(), menu.getId(), "order-" + i)));

        // then
        assertThat(count(results, Result::success)).isEqualTo(1);
        assertThat(balanceOf(user)).isZero();
        assertThat(orderRepository.count()).isEqualTo(1);
        assertThat(results.stream().filter(r -> !r.success())
                .allMatch(r -> r.error() instanceof BusinessException be
                        && be.getErrorCode() == ErrorCode.INSUFFICIENT_POINT)).isTrue();
    }

    @Test
    @DisplayName("같은 멱등키로 동시에 10번 충전하면 1건만 반영된다")
    void concurrentCharges_withSameIdempotencyKey_areAppliedOnce() throws Exception {
        // when
        List<Result> results = runConcurrently(10, i ->
                pointService.charge(new PointChargeRequestDto(user.getId(), 1_000L, "same-key")));

        // then
        assertThat(count(results, Result::success)).isEqualTo(1);
        assertThat(pointHistoryRepository.count()).isEqualTo(1);
        assertThat(balanceOf(user)).isEqualTo(1_000L);
    }

    @Test
    @DisplayName("서로 다른 키로 동시에 20번 충전해도 잔액 합계가 정확하다 (lost update 없음)")
    void concurrentCharges_withDistinctKeys_yieldExactTotal() throws Exception {
        // when
        List<Result> results = runConcurrently(20, i ->
                pointService.charge(new PointChargeRequestDto(user.getId(), 1_000L, "key-" + i)));

        // then
        assertThat(count(results, Result::success)).isEqualTo(20);
        assertThat(balanceOf(user)).isEqualTo(20_000L);
    }

    private long balanceOf(User u) {
        return userPointRepository.findById(u.getId()).orElseThrow().getBalance();
    }

    // ---- 헬퍼: 모든 스레드를 latch 로 세워뒀다가 동시에 출발시킨다 ----
    private record Result(boolean success, Throwable error) {}

    @FunctionalInterface
    private interface IndexedTask {
        void run(int index) throws Exception;
    }

    private List<Result> runConcurrently(int n, IndexedTask task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch ready = new CountDownLatch(n);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Result>> futures = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            int idx = i;
            futures.add(pool.submit(() -> {
                ready.countDown();
                start.await();
                try {
                    task.run(idx);
                    return new Result(true, null);
                } catch (Throwable t) {
                    return new Result(false, t);
                }
            }));
        }
        ready.await();
        start.countDown();

        List<Result> results = new ArrayList<>();
        for (Future<Result> f : futures) {
            results.add(f.get(30, TimeUnit.SECONDS));
        }
        pool.shutdown();
        return results;
    }

    private long count(List<Result> results, Predicate<Result> predicate) {
        return results.stream().filter(predicate).count();
    }
}
