package io.github.prjkmo112.cafeapi.domain.point.service;

import io.github.prjkmo112.cafeapi.common.exception.BusinessException;
import io.github.prjkmo112.cafeapi.common.exception.ErrorCode;
import io.github.prjkmo112.cafeapi.domain.point.dto.PointChargeRequestDto;
import io.github.prjkmo112.cafeapi.domain.point.dto.PointDto;
import io.github.prjkmo112.cafeapi.domain.point.entity.PointHistory;
import io.github.prjkmo112.cafeapi.domain.point.entity.PointHistoryType;
import io.github.prjkmo112.cafeapi.domain.point.entity.UserPoint;
import io.github.prjkmo112.cafeapi.domain.point.repository.PointHistoryRepository;
import io.github.prjkmo112.cafeapi.domain.point.repository.UserPointRepository;
import io.github.prjkmo112.cafeapi.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private UserPointRepository userPointRepository;

    @InjectMocks
    private PointService pointService;

    private UserPoint userPointWithBalance(long balance) {
        User user = User.builder().name("홍길동").email("hong@example.com").password("pw").build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
        UserPoint userPoint = UserPoint.builder().user(user).build();
        ReflectionTestUtils.setField(userPoint, "balance", balance);
        return userPoint;
    }

    @Nested
    @DisplayName("charge")
    class Charge {

        @Test
        @DisplayName("충전하면 잔액이 늘고 CHARGE 이력이 멱등키와 함께 저장된다")
        void success() {
            UserPoint userPoint = userPointWithBalance(1000L);
            given(pointHistoryRepository.findByUserIdAndIdempotencyKey(USER_ID, "key-1")).willReturn(Optional.empty());
            given(userPointRepository.findByUserIdForUpdate(USER_ID)).willReturn(Optional.of(userPoint));

            PointDto result = pointService.charge(new PointChargeRequestDto(USER_ID, 5000L, "key-1"));

            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.point()).isEqualTo(6000L);

            ArgumentCaptor<PointHistory> captor = ArgumentCaptor.forClass(PointHistory.class);
            verify(pointHistoryRepository).save(captor.capture());
            PointHistory saved = captor.getValue();
            assertThat(saved.getType()).isEqualTo(PointHistoryType.CHARGE);
            assertThat(saved.getAmount()).isEqualTo(5000L);
            assertThat(saved.getBalanceAfter()).isEqualTo(6000L);
            assertThat(saved.getIdempotencyKey()).isEqualTo("key-1");
        }

        @Test
        @DisplayName("이미 처리된 멱등키면 DUPLICATE_POINT_CHARGE_REQUEST 이고 잔액 조회/저장을 하지 않는다")
        void duplicateIdempotencyKey() {
            PointHistory existing = PointHistory.builder().type(PointHistoryType.CHARGE).build();
            given(pointHistoryRepository.findByUserIdAndIdempotencyKey(USER_ID, "key-1")).willReturn(Optional.of(existing));

            PointChargeRequestDto request = new PointChargeRequestDto(USER_ID, 5000L, "key-1");

            assertThatThrownBy(() -> pointService.charge(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.DUPLICATE_POINT_CHARGE_REQUEST);

            verify(userPointRepository, never()).findByUserIdForUpdate(any());
            verify(pointHistoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 MEMBER_NOT_FOUND")
        void userNotFound() {
            given(pointHistoryRepository.findByUserIdAndIdempotencyKey(USER_ID, "key-1")).willReturn(Optional.empty());
            given(userPointRepository.findByUserIdForUpdate(USER_ID)).willReturn(Optional.empty());

            PointChargeRequestDto request = new PointChargeRequestDto(USER_ID, 5000L, "key-1");

            assertThatThrownBy(() -> pointService.charge(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("선조회와 저장 사이에 동시 요청이 먼저 들어와 유니크 제약에 걸리면 DUPLICATE_POINT_CHARGE_REQUEST")
        void concurrentDuplicate() {
            UserPoint userPoint = userPointWithBalance(0L);
            given(pointHistoryRepository.findByUserIdAndIdempotencyKey(USER_ID, "key-1")).willReturn(Optional.empty());
            given(userPointRepository.findByUserIdForUpdate(USER_ID)).willReturn(Optional.of(userPoint));
            given(pointHistoryRepository.save(any(PointHistory.class)))
                    .willThrow(new DataIntegrityViolationException("uk_point_history_user_idempotency"));

            PointChargeRequestDto request = new PointChargeRequestDto(USER_ID, 5000L, "key-1");

            assertThatThrownBy(() -> pointService.charge(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.DUPLICATE_POINT_CHARGE_REQUEST);
        }

        @Test
        @DisplayName("충전 금액이 0 이하면 도메인 방어선에서 INVALID_POINT_AMOUNT 이고 이력을 남기지 않는다")
        void invalidAmount() {
            UserPoint userPoint = userPointWithBalance(1000L);
            given(pointHistoryRepository.findByUserIdAndIdempotencyKey(USER_ID, "key-1")).willReturn(Optional.empty());
            given(userPointRepository.findByUserIdForUpdate(USER_ID)).willReturn(Optional.of(userPoint));

            PointChargeRequestDto request = new PointChargeRequestDto(USER_ID, 0L, "key-1");

            assertThatThrownBy(() -> pointService.charge(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_POINT_AMOUNT);

            assertThat(userPoint.getBalance()).isEqualTo(1000L);
            verify(pointHistoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("use")
    class Use {

        @Test
        @DisplayName("사용하면 잔액이 차감되고 USE 이력이 저장된다")
        void success() {
            UserPoint userPoint = userPointWithBalance(10000L);
            given(userPointRepository.findByUserIdForUpdate(USER_ID)).willReturn(Optional.of(userPoint));

            PointDto result = pointService.use(USER_ID, 4000L);

            assertThat(result.point()).isEqualTo(6000L);

            ArgumentCaptor<PointHistory> captor = ArgumentCaptor.forClass(PointHistory.class);
            verify(pointHistoryRepository).save(captor.capture());
            PointHistory saved = captor.getValue();
            assertThat(saved.getType()).isEqualTo(PointHistoryType.USE);
            assertThat(saved.getAmount()).isEqualTo(4000L);
            assertThat(saved.getBalanceAfter()).isEqualTo(6000L);
            assertThat(saved.getIdempotencyKey()).isNull();
        }

        @Test
        @DisplayName("잔액과 같은 금액은 전액 사용할 수 있다")
        void useExactBalance() {
            UserPoint userPoint = userPointWithBalance(4000L);
            given(userPointRepository.findByUserIdForUpdate(USER_ID)).willReturn(Optional.of(userPoint));

            assertThat(pointService.use(USER_ID, 4000L).point()).isZero();
        }

        @Test
        @DisplayName("잔액이 부족하면 INSUFFICIENT_POINT 이고 잔액/이력은 변하지 않는다")
        void insufficient() {
            UserPoint userPoint = userPointWithBalance(3999L);
            given(userPointRepository.findByUserIdForUpdate(USER_ID)).willReturn(Optional.of(userPoint));

            assertThatThrownBy(() -> pointService.use(USER_ID, 4000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INSUFFICIENT_POINT);

            assertThat(userPoint.getBalance()).isEqualTo(3999L);
            verify(pointHistoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 MEMBER_NOT_FOUND")
        void userNotFound() {
            given(userPointRepository.findByUserIdForUpdate(USER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> pointService.use(USER_ID, 4000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

}
