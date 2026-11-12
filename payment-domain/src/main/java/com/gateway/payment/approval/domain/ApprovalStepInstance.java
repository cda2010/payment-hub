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
import jakarta.persistence.PreUpdate;
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
@Table(name = "approval_step_instance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalStepInstance {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_instance_id", nullable = false)
    private ApprovalInstance approvalInstance;

    @Column(name = "level_number", nullable = false)
    private int levelNumber;

    @Column(name = "required_approvals", nullable = false)
    @Builder.Default
    private int requiredApprovals = 1;

    @Column(name = "collected_approvals", nullable = false)
    @Builder.Default
    private int collectedApprovals = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ApprovalStepStatus status;

    /** JSON assignments [{type,value}] */
    @Column(name = "assignments_json", columnDefinition = "TEXT")
    private String assignmentsJson;

    /** Groupes candidats Flowable (CSV). */
    @Column(name = "candidate_groups", length = 500)
    private String candidateGroups;

    @Column(name = "bpm_task_definition_key", length = 80)
    private String bpmTaskDefinitionKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (status == null) {
            status = ApprovalStepStatus.PENDING;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
