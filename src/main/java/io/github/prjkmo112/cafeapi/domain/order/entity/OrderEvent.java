package io.github.prjkmo112.cafeapi.domain.order.entity;

import io.github.prjkmo112.cafeapi.common.entity.AuditingEntity;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Getter
@Entity
@Table(name = "order_event", indexes = {@Index(name = "idx_order_event_status_id",
        columnList = "status, id")}, uniqueConstraints = {@UniqueConstraint(name = "uk_order_event_order_type",
        columnNames = {
                "order_id",
                "event_type"})})
public class OrderEvent extends AuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Size(max = 30)
    @NotNull
    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    private Map<String, Object> payload;

    @Size(max = 20)
    @NotNull
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @NotNull
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "published_at")
    private Instant publishedAt;


}