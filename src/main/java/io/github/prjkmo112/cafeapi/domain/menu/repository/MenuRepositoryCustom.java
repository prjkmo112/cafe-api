package io.github.prjkmo112.cafeapi.domain.menu.repository;

import io.github.prjkmo112.cafeapi.domain.menu.dto.MenuDto;
import io.github.prjkmo112.cafeapi.domain.menu.entity.MenuStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface MenuRepositoryCustom {

    Page<MenuDto> findAllMenus(
            String keyword,
            Long priceStart,
            Long priceEnd,
            MenuStatus status,
            LocalDateTime createdAtStart,
            LocalDateTime createdAtEnd,
            Pageable pageable
    );

}
