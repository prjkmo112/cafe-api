package io.github.prjkmo112.cafeapi.domain.order.repository;

import io.github.prjkmo112.cafeapi.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}