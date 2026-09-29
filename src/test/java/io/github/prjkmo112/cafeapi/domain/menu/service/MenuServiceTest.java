package io.github.prjkmo112.cafeapi.domain.menu.service;

import io.github.prjkmo112.cafeapi.common.exception.BusinessException;
import io.github.prjkmo112.cafeapi.common.exception.ErrorCode;
import io.github.prjkmo112.cafeapi.domain.menu.dto.MenuDto;
import io.github.prjkmo112.cafeapi.domain.menu.dto.MenuListRequestDto;
import io.github.prjkmo112.cafeapi.domain.menu.dto.PopularMenuDto;
import io.github.prjkmo112.cafeapi.domain.menu.entity.Menu;
import io.github.prjkmo112.cafeapi.domain.menu.entity.MenuStatus;
import io.github.prjkmo112.cafeapi.domain.menu.repository.MenuRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    @DisplayName("메뉴 목록 조회 시 검색 조건이 그대로 Repository 에 전달된다")
    void getMenuList_passesFiltersToRepository() {
        MenuListRequestDto request = new MenuListRequestDto();
        request.setKeyword("라떼");
        request.setPriceStart(4000L);
        request.setPriceEnd(5000L);
        request.setStatus(MenuStatus.SALE);
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 30, 0, 0);
        request.setCreatedAtStart(start);
        request.setCreatedAtEnd(end);
        Pageable pageable = PageRequest.of(0, 10);

        Page<MenuDto> expected = new PageImpl<>(List.of(
                new MenuDto(2L, "카페라떼", 4500L, MenuStatus.SALE, start)), pageable, 1);
        given(menuRepository.findAllMenus("라떼", 4000L, 5000L, MenuStatus.SALE, start, end, pageable))
                .willReturn(expected);

        Page<MenuDto> result = menuService.getMenuList(request, pageable);

        assertThat(result).isSameAs(expected);
        assertThat(result.getContent()).extracting(MenuDto::name).containsExactly("카페라떼");
    }

    @Test
    @DisplayName("조건이 없으면 null 필터로 전체 조회를 위임한다")
    void getMenuList_withoutFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        given(menuRepository.findAllMenus(null, null, null, null, null, null, pageable))
                .willReturn(Page.empty(pageable));

        Page<MenuDto> result = menuService.getMenuList(new MenuListRequestDto(), pageable);

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("존재하는 메뉴를 조회하면 메뉴를 반환한다")
    void getMenu_success() {
        Menu menu = Menu.builder().name("아메리카노").price(4000L).status(MenuStatus.SALE).build();
        given(menuRepository.findById(1L)).willReturn(Optional.of(menu));

        assertThat(menuService.getMenu(1L)).isSameAs(menu);
    }

    @Test
    @DisplayName("존재하지 않는 메뉴를 조회하면 PRODUCT_NOT_FOUND 예외가 발생한다")
    void getMenu_notFound() {
        given(menuRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.getMenu(99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("인기 메뉴 조회 시 지정한 일수 이전 시점부터 집계하도록 위임한다")
    void getPopularMenus_usesStartDateFromBeforeDays() {
        Pageable pageable = Pageable.ofSize(3);
        List<PopularMenuDto> expected = List.of(new PopularMenuDto(1L, "아메리카노", 4000L, 26L));
        given(menuRepository.findPopularMenusByDate(any(LocalDateTime.class), eq(pageable)))
                .willReturn(expected);

        LocalDateTime before = LocalDateTime.now().minusDays(7);
        List<PopularMenuDto> result = menuService.getPopularMenus(7, pageable);
        LocalDateTime after = LocalDateTime.now().minusDays(7);

        assertThat(result).isEqualTo(expected);

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(menuRepository).findPopularMenusByDate(captor.capture(), eq(pageable));
        assertThat(captor.getValue()).isBetween(before, after);
    }

}
