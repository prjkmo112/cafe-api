package io.github.prjkmo112.cafeapi.domain.order.facade;

import io.github.prjkmo112.cafeapi.common.exception.BusinessException;
import io.github.prjkmo112.cafeapi.common.exception.ErrorCode;
import io.github.prjkmo112.cafeapi.domain.menu.entity.Menu;
import io.github.prjkmo112.cafeapi.domain.menu.entity.MenuStatus;
import io.github.prjkmo112.cafeapi.domain.menu.service.MenuService;
import io.github.prjkmo112.cafeapi.domain.order.dto.CreateOrderRequestDto;
import io.github.prjkmo112.cafeapi.domain.order.dto.OrderDto;
import io.github.prjkmo112.cafeapi.domain.order.service.OrderService;
import io.github.prjkmo112.cafeapi.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final MenuService menuService;
    private final PointService pointService;

    @Transactional
    public OrderDto createOrder(CreateOrderRequestDto createOrderRequestDto) {
        // 메뉴 조회
        Menu menu = menuService.getMenu(createOrderRequestDto.menuId());

        if (menu.getStatus() == MenuStatus.SOLDOUT) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }

        // 포인트 차감
        pointService.use(createOrderRequestDto.userId(), menu.getPrice());

        // 주문 생성
        return orderService.createOrder(createOrderRequestDto, menu);
    }

}
