package io.github.prjkmo112.cafeapi.domain.menu.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.prjkmo112.cafeapi.common.exception.BusinessException;
import io.github.prjkmo112.cafeapi.common.exception.ErrorCode;
import io.github.prjkmo112.cafeapi.domain.menu.dto.MenuDto;
import io.github.prjkmo112.cafeapi.domain.menu.entity.MenuStatus;
import io.github.prjkmo112.cafeapi.domain.menu.entity.QMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final Map<String, ComparableExpressionBase<?>> SORTABLE_FIELDS = Map.of(
            "name", QMenu.menu.name,
            "price", QMenu.menu.price,
            "status", QMenu.menu.status,
            "createdAt", QMenu.menu.createdAt
    );

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
                .orderBy(toOrderSpecifiers(pageable.getSort()))
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

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        // 정렬 가능한 컬럼 거르기
        for (Sort.Order order : sort) {
            ComparableExpressionBase<?> field = SORTABLE_FIELDS.get(order.getProperty());
            if (field == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "지원되지 않는 정렬 컬럼입니다");
            }
            orders.add(order.isAscending() ? field.asc() : field.desc());
        }

        // 아무 정렬 조건없이 들어와도 무조건 default 로 id 로 정렬 고정
        // 매번 요청하더라도 정렬 기준 고정되도록
        orders.add(QMenu.menu.id.asc());

        return orders.toArray(new OrderSpecifier[0]);
    }

}
