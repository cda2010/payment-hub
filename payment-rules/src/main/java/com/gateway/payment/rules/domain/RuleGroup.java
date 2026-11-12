package com.gateway.payment.rules.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rule_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleGroup {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 200)
    private String libelle;

    @Column(length = 500)
    private String description;

    @Column(name = "transaction_type", length = 64)
    private String transactionType;

    @Column(name = "product_code", length = 80)
    private String productCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_strategy", nullable = false, length = 32)
    private EvaluationStrategy evaluationStrategy;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<RuleGroupItem> items = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (evaluationStrategy == null) {
            evaluationStrategy = EvaluationStrategy.STOP_ON_REJECT;
        }
        if (status == null) {
            status = "ACTIVE";
        }
    }
}
