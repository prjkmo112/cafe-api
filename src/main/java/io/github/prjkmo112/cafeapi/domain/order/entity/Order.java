package io.github.prjkmo112.cafeapi.domain.order.entity;

import io.github.prjkmo112.cafeapi.common.entity.AuditingEntity;
import io.github.prjkmo112.cafeapi.domain.user.entity.User;
import io.github.prjkmo112.cafeapi.domain.menu.entity.Menu;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Entity
@Table(name = "orders", indexes = {@Index(name = "idx_orders_status_created_menu",
        columnList = "status, created_at, menu_id")}, uniqueConstraints = {@UniqueConstraint(name = "uk_orders_user_idempotency",
        columnNames = {
                "user_id",
                "idempotency_key"})})
public class Order extends AuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @NotNull
    @Column(name = "paid_amount", nullable = false)
    private Long paidAmount;

    @Size(max = 20)
    @NotNull
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Size(max = 64)
    @NotNull
    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;

    @Size(max = 64)
    @NotNull
    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;


}