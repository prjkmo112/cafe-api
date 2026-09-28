package io.github.prjkmo112.cafeapi.domain.point.entity;

import io.github.prjkmo112.cafeapi.common.entity.AuditingEntity;
import io.github.prjkmo112.cafeapi.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "point_history", indexes = {@Index(name = "idx_point_history_user_created",
        columnList = "user_id, created_at")}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_point_history_user_idempotency",
                columnNames = {
                        "user_id",
                        "idempotency_key"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory extends AuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private PointHistoryType type;

    @NotNull
    @Column(name = "amount", nullable = false)
    private Long amount;

    @NotNull
    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @Size(max = 64)
    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Builder
    private PointHistory(User user, PointHistoryType type, Long amount, Long balanceAfter, String idempotencyKey) {
        this.user = user;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.idempotencyKey = idempotencyKey;
    }

}