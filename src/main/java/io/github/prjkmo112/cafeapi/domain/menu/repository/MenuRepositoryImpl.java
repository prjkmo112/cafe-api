package io.github.prjkmo112.cafeapi.domain.menu.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.prjkmo112.cafeapi.domain.menu.dto.MenuDto;
import io.github.prjkmo112.cafeapi.domain.menu.entity.MenuStatus;
import io.github.prjkmo112.cafeapi.domain.menu.entity.QMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MenuDto> findAllMenus(
            String keyword,
            Long priceStart,
            Long priceEnd,
            MenuStatus status,
            LocalDateTime createdAtStart,
            LocalDateTime createdAtEnd,
            Pageable pageable
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(nameContains(keyword))
                .and(priceBetween(priceStart, priceEnd))
                .and(statusEq(status))
                .and(createdAtBetween(createdAtStart, createdAtEnd));

        List<MenuDto> menus = queryFactory
                .select(Projections.constructor(MenuDto.class,
                        QMenu.menu.id,
                        QMenu.menu.name,
                        QMenu.menu.price,
                        QMenu.menu.status,
                        QMenu.menu.createdAt
                ))
                .from(QMenu.menu)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(QMenu.menu.count())
                .from(QMenu.menu)
                .where(builder);

        return new PageImpl<>(menus, pageable, countQuery.fetchOne());
    }

    private BooleanExpression nameContains(String keyword) {
        return StringUtils.hasText(keyword) ? QMenu.menu.name.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression priceBetween(Long st, Long end) {
        if (st == null && end == null) return null;
        return QMenu.menu.price.between(st, end);
    }

    private BooleanExpression statusEq(MenuStatus status) {
        return status != null ? QMenu.menu.status.eq(status) : null;
    }

    private BooleanExpression createdAtBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) return null;
        return QMenu.menu.createdAt.between(start, end);
    }

}
