package com.gateway.payment.approval.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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

/**
 * Instance d'approbation liée à un paiement (snapshot policy + profile).
 */
@Entity
@Table(name = "approval_instance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalInstance {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "payment_id", nullable = false, unique = true, length = 64)
    private String paymentId;

    @Column(name = "policy_code", length = 80)
    private String policyCode;

    @Column(name = "profile_code", nullable = false, length = 80)
    private String profileCode;

    @Column(name = "strategy", length = 40)
    @Builder.Default
    private String strategy = "SEQUENTIAL";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ApprovalInstanceStatus status;

    @Column(name = "current_level")
    private Integer currentLevel;

    @Column(name = "total_levels", nullable = false)
    @Builder.Default
    private int totalLevels = 0;

    /** Snapshot JSON de la policy appliquée. */
    @Column(name = "policy_snapshot_json", columnDefinition = "TEXT")
    private String policySnapshotJson;

    /** Snapshot JSON du profile résolu (levels + assignments). */
    @Column(name = "profile_snapshot_json", columnDefinition = "TEXT")
    private String profileSnapshotJson;

    /** Empreinte contexte critique (montant, devise, bénéficiaire…). */
    @Column(name = "context_fingerprint", length = 128)
    private String contextFingerprint;

    @Column(name = "initiated_by", length = 120)
    private String initiatedBy;

    @Column(name = "dmn_decision_key", length = 120)
    private String dmnDecisionKey;

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
            status = ApprovalInstanceStatus.PENDING;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
