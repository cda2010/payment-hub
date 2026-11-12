package com.gateway.payment.orchestration.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Ligne de schéma = step BPMN / jambe FSE ordonnée. */
@Entity
@Table(
        name = "ligne_schema_ecriture",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ligne_schema_ordre",
                columnNames = {"schema_ecriture_id", "ordre"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneSchemaEcriture {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schema_ecriture_id", nullable = false)
    private SchemaEcriture schemaEcriture;

    @Column(nullable = false)
    private Integer ordre;

    @Column(name = "step_id", length = 128)
    private String stepId;

    @Column(name = "type_methode", nullable = false, length = 64)
    private String typeMethode;

    @Enumerated(EnumType.STRING)
    @Column(name = "api_a_utiliser", nullable = false, length = 32)
    private ProprieteApi apiAUtiliser;

    @Enumerated(EnumType.STRING)
    @Column(name = "agence_imputer", length = 32)
    private ImputeAgence agenceImputer;

    @Column(name = "execution_mode", length = 32)
    private String executionMode;

    @Column(name = "timeout_policy", length = 64)
    private String timeoutPolicy;

    @Column(name = "timeout_duration", length = 64)
    private String timeoutDuration;

    @Column(name = "retry_policy", length = 64)
    private String retryPolicy;

    @Column(name = "callback_policy", length = 64)
    private String callbackPolicy;

    @Column(name = "status_method", length = 64)
    private String statusMethod;

    @Column(name = "retry_max_attempts")
    private Integer retryMaxAttempts;

    @Column(name = "retry_initial_delay_ms")
    private Long retryInitialDelayMs;

    @Column(name = "retry_multiplier")
    private Double retryMultiplier;

    @Column(name = "retry_max_delay_ms")
    private Long retryMaxDelayMs;

    @Column(name = "retryable_codes", length = 512)
    private String retryableCodes;

    @Column(name = "idempotency_policy", length = 64)
    private String idempotencyPolicy;

    @Column(name = "compensation_policy", length = 64)
    private String compensationPolicy;

    @Column(name = "compensation_mode", length = 48)
    private String compensationMode;

    @Column(name = "compensation_step_code", length = 128)
    private String compensationStepCode;

    @Column(name = "compensation_reverse_desc")
    private Boolean compensationReverseDesc;

    @Column(name = "compensation_max_attempts")
    private Integer compensationMaxAttempts;

    @Column(name = "compensation_manual_review")
    private Boolean compensationManualReview;

    @Column(name = "depends_on_step_code", length = 128)
    private String dependsOnStepCode;

    @Column(name = "dependency_condition", length = 48)
    private String dependencyCondition;

    @Column(name = "dependency_required")
    private Boolean dependencyRequired;

    @Column(name = "error_policies", columnDefinition = "text")
    private String errorPolicies;

    @Column(name = "error_policy_code", length = 64)
    private String errorPolicyCode;

    @Column(name = "default_error_action", length = 48)
    private String defaultErrorAction;

    @Column(name = "check_status_method_code", length = 64)
    private String checkStatusMethodCode;

    @Column(name = "max_retries")
    private Integer maxRetries;

    private Boolean blocking;

    @Column(length = 32)
    private String criticality;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "ligneSchemaEcriture", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ParametreSchema> parametres = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
