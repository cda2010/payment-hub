package com.gateway.payment.rules.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rule_execution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleExecution {

    @Id
    private UUID id;

    @Column(name = "transaction_id", nullable = false, length = 80)
    private String transactionId;

    @Column(name = "group_code", length = 64)
    private String groupCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RuleDecision decision;

    @Column(nullable = false)
    private boolean authorized;

    @Column(nullable = false)
    private boolean simulate;

    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "request_json", columnDefinition = "TEXT")
    private String requestJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "execution", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RuleViolation> violations = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
