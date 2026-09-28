package io.github.prjkmo112.cafeapi.domain.menu.controller;

import io.github.prjkmo112.cafeapi.common.dto.ApiResponse;
import io.github.prjkmo112.cafeapi.common.dto.PageResponse;
import io.github.prjkmo112.cafeapi.domain.menu.dto.MenuDto;
import io.github.prjkmo112.cafeapi.domain.menu.dto.MenuListRequestDto;
import io.github.prjkmo112.cafeapi.domain.menu.dto.PopularMenuDto;
import io.github.prjkmo112.cafeapi.domain.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ApiResponse<PageResponse<MenuDto>> getMenuList(
            @ModelAttribute MenuListRequestDto menuListRequestDto,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<MenuDto> menuListPage = menuService.getMenuList(menuListRequestDto, pageable);
        return ApiResponse.ok(PageResponse.from(menuListPage));
    }

    @GetMapping("/popular")
    public ApiResponse<List<PopularMenuDto>> getPopularMenus() {
        return ApiResponse.ok(menuService.getPopularMenus(7, Pageable.ofSize(3)));
    }

}
