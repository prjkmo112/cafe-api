package io.github.prjkmo112.cafeapi.domain.order.dto;

import io.github.prjkmo112.cafeapi.domain.order.entity.Order;
import io.github.prjkmo112.cafeapi.domain.order.entity.OrderStatus;

public record OrderDto(
        Long memberId,
        Long menuId,
        String orderId,
        Long amount,
        OrderStatus status
) {

    public static OrderDto from(Order order) {
        return new OrderDto(
                order.getUser().getId(),
                order.getMenu().getId(),
                order.getOrderId(),
                order.getAmount(),
                order.getStatus()
        );
    }

}
