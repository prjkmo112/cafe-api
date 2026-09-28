package io.github.prjkmo112.cafeapi.common.dto.event;

import io.github.prjkmo112.cafeapi.domain.order.entity.Order;
import lombok.Builder;

@Builder
public record OrderPaidEvent(
        Long userId,
        Long menuId,
        Long paidAmount,
        String orderId
) {

    public static OrderPaidEvent from(Order order) {
        return OrderPaidEvent.builder()
                .userId(order.getUser().getId())
                .menuId(order.getMenu().getId())
                .paidAmount(order.getAmount())
                .orderId(order.getOrderId())
                .build();
    }

}
