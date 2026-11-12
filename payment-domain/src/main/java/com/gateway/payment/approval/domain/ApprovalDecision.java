package com.gateway.payment.approval.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "approval_decision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalDecision {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_step_instance_id", nullable = false)
    private ApprovalStepInstance stepInstance;

    @Column(name = "approver_id", nullable = false, length = 120)
    private String approverId;

    @Column(name = "approver_role", length = 120)
    private String approverRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalDecisionOutcome outcome;

    @Column(length = 1000)
    private String comment;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    @PrePersist
    void onCreate() {
        if (decidedAt == null) {
            decidedAt = Instant.now();
        }
    }
}
