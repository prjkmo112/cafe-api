package io.github.prjkmo112.cafeapi.domain.user.repository;

import io.github.prjkmo112.cafeapi.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}