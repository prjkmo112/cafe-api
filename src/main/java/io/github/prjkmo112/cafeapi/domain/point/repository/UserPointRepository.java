package io.github.prjkmo112.cafeapi.domain.point.repository;

import io.github.prjkmo112.cafeapi.domain.point.entity.UserPoint;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserPoint u WHERE u.user.id = :userId")
    Optional<UserPoint> findByUserIdForUpdate(@Param("userId") Long userId);

}