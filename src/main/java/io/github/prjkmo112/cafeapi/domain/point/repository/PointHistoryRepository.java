package io.github.prjkmo112.cafeapi.domain.point.repository;

import io.github.prjkmo112.cafeapi.domain.point.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    Optional<PointHistory> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
}