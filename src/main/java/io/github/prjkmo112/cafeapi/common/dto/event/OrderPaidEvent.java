package io.github.prjkmo112.cafeapi.common.dto.event;

import lombok.Builder;

@Builder
public record OrderPaidEvent(
        Long userId,
        Long menuId,
        Long paidAmount,
        String orderId
) {
}
