package io.github.prjkmo112.cafeapi.domain.point.repository;

import io.github.prjkmo112.cafeapi.domain.point.entity.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {
}