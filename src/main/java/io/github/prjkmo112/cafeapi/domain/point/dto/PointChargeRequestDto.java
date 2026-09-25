package io.github.prjkmo112.cafeapi.domain.point.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PointChargeRequestDto(
        @NotNull
        @Min(1)
        Long userId,

        @NotNull
        @Min(1)
        Long point,

        @NotBlank
        String idempotencyKey
) {
}
