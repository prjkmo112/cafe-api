package io.github.prjkmo112.cafeapi.domain.order.service;

import io.github.prjkmo112.cafeapi.common.dto.event.OrderPaidEvent;
import io.github.prjkmo112.cafeapi.domain.menu.entity.Menu;
import io.github.prjkmo112.cafeapi.domain.menu.entity.MenuStatus;
import io.github.prjkmo112.cafeapi.domain.order.dto.CreateOrderRequestDto;
import io.github.prjkmo112.cafeapi.domain.order.dto.OrderDto;
import io.github.prjkmo112.cafeapi.domain.order.entity.Order;
import io.github.prjkmo112.cafeapi.domain.order.entity.OrderStatus;
import io.github.prjkmo112.cafeapi.domain.order.repository.OrderRepository;
import io.github.prjkmo112.cafeapi.domain.user.entity.User;
import io.github.prjkmo112.cafeapi.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("주문을 저장하고 OrderPaidEvent 를 발행하며 PAID 상태의 OrderDto 를 반환한다")
    void createOrder_success() {
        User user = User.builder().name("홍길동").email("hong@example.com").password("pw").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Menu menu = Menu.builder().name("아메리카노").price(4000L).status(MenuStatus.SALE).build();
        ReflectionTestUtils.setField(menu, "id", 10L);
        given(userRepository.getReferenceById(1L)).willReturn(user);

        OrderDto result = orderService.createOrder(new CreateOrderRequestDto(1L, 10L), menu);

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.menuId()).isEqualTo(10L);
        assertThat(result.amount()).isEqualTo(4000L);
        assertThat(result.status()).isEqualTo(OrderStatus.PAID);
        assertThat(result.orderId()).startsWith("ORD-");

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getAmount()).isEqualTo(4000L);

        ArgumentCaptor<OrderPaidEvent> eventCaptor = ArgumentCaptor.forClass(OrderPaidEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        OrderPaidEvent event = eventCaptor.getValue();
        assertThat(event.userId()).isEqualTo(1L);
        assertThat(event.menuId()).isEqualTo(10L);
        assertThat(event.paidAmount()).isEqualTo(4000L);
        assertThat(event.orderId()).isEqualTo(result.orderId());
    }

}
