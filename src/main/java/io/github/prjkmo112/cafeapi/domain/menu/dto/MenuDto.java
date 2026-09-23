package io.github.prjkmo112.cafeapi.domain.menu.dto;

import io.github.prjkmo112.cafeapi.domain.menu.entity.Menu;
import io.github.prjkmo112.cafeapi.domain.menu.entity.MenuStatus;

import java.time.LocalDateTime;

public record MenuDto(
        Long id,
        String name,
        Long price,
        MenuStatus status,
        LocalDateTime createdAt
) {

    public static MenuDto from(Menu menu) {
        return new MenuDto(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getStatus(),
                menu.getCreatedAt()
        );
    }

}
