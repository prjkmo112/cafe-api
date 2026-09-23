package io.github.prjkmo112.cafeapi.domain.menu.repository;

import io.github.prjkmo112.cafeapi.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long>, MenuRepositoryCustom {
}