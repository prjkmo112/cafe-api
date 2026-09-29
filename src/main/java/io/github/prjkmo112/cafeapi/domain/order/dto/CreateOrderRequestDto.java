package io.github.prjkmo112.cafeapi.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderRequestDto(
        @NotNull
        @Min(1)
        Long userId,

        @NotNull
        @Min(1)
        Long menuId,

        @NotBlank
        @Size(max = 64)
        String idempotencyKey
) {
}
