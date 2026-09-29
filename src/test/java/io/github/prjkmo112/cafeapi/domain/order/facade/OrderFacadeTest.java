package io.github.prjkmo112.cafeapi.domain.order.facade;

import io.github.prjkmo112.cafeapi.common.exception.BusinessException;
import io.github.prjkmo112.cafeapi.common.exception.ErrorCode;
import io.github.prjkmo112.cafeapi.domain.menu.entity.Menu;
import io.github.prjkmo112.cafeapi.domain.menu.entity.MenuStatus;
import io.github.prjkmo112.cafeapi.domain.menu.service.MenuService;
import io.github.prjkmo112.cafeapi.domain.order.dto.CreateOrderRequestDto;
import io.github.prjkmo112.cafeapi.domain.order.dto.OrderDto;
import io.github.prjkmo112.cafeapi.domain.order.entity.OrderStatus;
import io.github.prjkmo112.cafeapi.domain.order.service.OrderService;
import io.github.prjkmo112.cafeapi.domain.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    private static final CreateOrderRequestDto REQUEST = new CreateOrderRequestDto(1L, 10L);

    @Mock
    private OrderService orderService;

    @Mock
    private MenuService menuService;

    @Mock
    private PointService pointService;

    @InjectMocks
    private OrderFacade orderFacade;

    private Menu menu(MenuStatus status) {
        return Menu.builder().name("아메리카노").price(4000L).status(status).build();
    }

    @Test
    @DisplayName("판매중 메뉴는 메뉴 조회 → 포인트 차감 → 주문 생성 순서로 처리된다")
    void createOrder_success() {
        Menu menu = menu(MenuStatus.SALE);
        OrderDto expected = new OrderDto(1L, 10L, "ORD-1", 4000L, OrderStatus.PAID);
        given(menuService.getMenu(10L)).willReturn(menu);
        given(orderService.createOrder(REQUEST, menu)).willReturn(expected);

        OrderDto result = orderFacade.createOrder(REQUEST);

        assertThat(result).isSameAs(expected);
        InOrder inOrder = inOrder(menuService, pointService, orderService);
        inOrder.verify(menuService).getMenu(10L);
        inOrder.verify(pointService).use(1L, 4000L);
        inOrder.verify(orderService).createOrder(REQUEST, menu);
    }

    @Test
    @DisplayName("품절 메뉴는 INSUFFICIENT_STOCK 이고 포인트 차감/주문 생성을 하지 않는다")
    void createOrder_soldOut() {
        given(menuService.getMenu(10L)).willReturn(menu(MenuStatus.SOLDOUT));

        assertThatThrownBy(() -> orderFacade.createOrder(REQUEST))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_STOCK);

        verifyNoInteractions(pointService, orderService);
    }

    @Test
    @DisplayName("존재하지 않는 메뉴면 PRODUCT_NOT_FOUND 가 전파되고 이후 단계는 실행되지 않는다")
    void createOrder_menuNotFound() {
        given(menuService.getMenu(10L)).willThrow(new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        assertThatThrownBy(() -> orderFacade.createOrder(REQUEST))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);

        verifyNoInteractions(pointService, orderService);
    }

    @Test
    @DisplayName("포인트가 부족하면 INSUFFICIENT_POINT 가 전파되고 주문은 생성되지 않는다")
    void createOrder_insufficientPoint() {
        given(menuService.getMenu(10L)).willReturn(menu(MenuStatus.SALE));
        given(pointService.use(1L, 4000L)).willThrow(new BusinessException(ErrorCode.INSUFFICIENT_POINT));

        assertThatThrownBy(() -> orderFacade.createOrder(REQUEST))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_POINT);

        verify(orderService, never()).createOrder(any(), any());
    }

}
