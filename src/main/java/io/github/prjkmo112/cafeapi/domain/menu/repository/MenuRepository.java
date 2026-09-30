package io.github.prjkmo112.cafeapi.domain.menu.repository;

import io.github.prjkmo112.cafeapi.domain.menu.dto.PopularMenuDto;
import io.github.prjkmo112.cafeapi.domain.menu.entity.Menu;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long>, MenuRepositoryCustom {


    @Query("""
           SELECT new io.github.prjkmo112.cafeapi.domain.menu.dto.PopularMenuDto(
                      m.id,
                      m.name,
                      m.price,
                      COUNT(o.id)
          )
           FROM Order o
           JOIN o.menu m
           WHERE o.status NOT IN ('CANCELED')
           AND o.createdAt >= :startDate
           GROUP BY m.id, m.name, m.price
           ORDER BY COUNT(o.id) DESC, m.id ASC
          """)
    List<PopularMenuDto> findPopularMenusByDate(
            @Param("startDate") LocalDateTime startDate,
            Pageable pageable
    );

}