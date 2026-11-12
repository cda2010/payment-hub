package com.gateway.payment.rules.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "rule_version")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleVersion {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private RuleDefinition rule;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false)
    private int priority;

    @Column(name = "conditions_json", nullable = false, columnDefinition = "TEXT")
    private String conditionsJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 40)
    private SourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_period", length = 32)
    private LimitPeriod sourcePeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_scope", length = 32)
    private LimitScope sourceScope;

    @Column(name = "include_current", nullable = false)
    private boolean includeCurrent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RuleOperator operator;

    @Column(name = "limit_value", precision = 18, scale = 4)
    private BigDecimal limitValue;

    @Column(name = "limit_values_json", columnDefinition = "TEXT")
    private String limitValuesJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_on_violation", nullable = false, length = 16)
    private RuleDecision actionOnViolation;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private boolean mandatory;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RuleStatus status;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (conditionsJson == null) {
            conditionsJson = "{}";
        }
        if (status == null) {
            status = RuleStatus.BROUILLON;
        }
        if (actionOnViolation == null) {
            actionOnViolation = RuleDecision.REJECT;
        }
        if (effectiveFrom == null) {
            effectiveFrom = LocalDate.now();
        }
    }

    public boolean isEffectiveOn(LocalDate day) {
        if (effectiveFrom != null && day.isBefore(effectiveFrom)) {
            return false;
        }
        return effectiveTo == null || !day.isAfter(effectiveTo);
    }
}
