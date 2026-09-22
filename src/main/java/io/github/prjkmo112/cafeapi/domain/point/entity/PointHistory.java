package io.github.prjkmo112.cafeapi.domain.point.entity;

import io.github.prjkmo112.cafeapi.common.entity.AuditingEntity;
import io.github.prjkmo112.cafeapi.domain.order.entity.Order;
import io.github.prjkmo112.cafeapi.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Entity
@Table(name = "point_history", indexes = {@Index(name = "idx_point_history_user_created",
        columnList = "user_id, created_at")}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_point_history_user_idempotency",
                columnNames = {
                        "user_id",
                        "idempotency_key"}),
        @UniqueConstraint(name = "uk_point_history_order",
                columnNames = {"order_id"})})
public class PointHistory extends AuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 10)
    @NotNull
    @Column(name = "type", nullable = false, length = 10)
    private String type;

    @NotNull
    @Column(name = "amount", nullable = false)
    private Long amount;

    @NotNull
    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Size(max = 64)
    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Size(max = 64)
    @Column(name = "request_hash", length = 64)
    private String requestHash;


}