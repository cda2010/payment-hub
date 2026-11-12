package com.gateway.payment.rules.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "rule_violation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleViolation {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "execution_id", nullable = false)
    private RuleExecution execution;

    @Column(name = "rule_code", nullable = false, length = 64)
    private String ruleCode;

    @Column(name = "rule_version", nullable = false)
    private int ruleVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RuleDecision action;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "current_value", precision = 18, scale = 4)
    private BigDecimal currentValue;

    @Column(name = "requested_value", precision = 18, scale = 4)
    private BigDecimal requestedValue;

    @Column(name = "projected_value", precision = 18, scale = 4)
    private BigDecimal projectedValue;

    @Column(name = "limit_value", precision = 18, scale = 4)
    private BigDecimal limitValue;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
