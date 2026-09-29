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

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final MenuService menuService;
    private final PointService pointService;

    @Transactional
    public OrderDto createOrder(CreateOrderRequestDto createOrderRequestDto) {
        // 멱등키 선조회: 이미 처리된 재시도 요청은 기존 주문을 그대로 반환
        Optional<OrderDto> orderDto = orderService.findByIdempotencyKey(
                createOrderRequestDto.userId(),
                createOrderRequestDto.idempotencyKey()
        );
        if (orderDto.isPresent()) {
            return orderDto.get();
        }

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
