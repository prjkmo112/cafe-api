package io.github.prjkmo112.cafeapi.domain.menu.service;

import io.github.prjkmo112.cafeapi.common.exception.BusinessException;
import io.github.prjkmo112.cafeapi.common.exception.ErrorCode;
import io.github.prjkmo112.cafeapi.domain.menu.dto.MenuDto;
import io.github.prjkmo112.cafeapi.domain.menu.dto.MenuListRequestDto;
import io.github.prjkmo112.cafeapi.domain.menu.entity.Menu;
import io.github.prjkmo112.cafeapi.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;

    public Page<MenuDto> getMenuList(MenuListRequestDto menuListRequestDto, Pageable pageable) {
        return menuRepository.findAllMenus(
                menuListRequestDto.getKeyword(),
                menuListRequestDto.getPriceStart(),
                menuListRequestDto.getPriceEnd(),
                menuListRequestDto.getStatus(),
                menuListRequestDto.getCreatedAtStart(),
                menuListRequestDto.getCreatedAtEnd(),
                pageable
        );
    }

    public Menu getMenu(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

}
