package io.github.prjkmo112.cafeapi.domain.menu.dto;

public record PopularMenuDto(
        Long menuId,
        String name,
        Long price,
        Long count
) {
}
