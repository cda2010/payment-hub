package com.gateway.payment.orchestration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Règle d'erreur groupée par {@code code} (= {@link LigneSchemaEcriture#getErrorPolicyCode()}).
 * {@code errorType} / {@code action} sont des codes alignés sur les enums omnicanal policy.
 */
@Entity
@Table(name = "step_error_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StepErrorPolicy {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "code", length = 64, nullable = false)
    private String code;

    @Column(name = "error_type", length = 48, nullable = false)
    private String errorType;

    @Column(name = "action", length = 48, nullable = false)
    private String action;

    @Column(name = "max_retries")
    private Integer maxRetries;

    @Column(name = "retry_delay_ms")
    private Long retryDelayMs;

    @Column(name = "target_step_code", length = 128)
    private String targetStepCode;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Portée schéma optionnelle (UUID schema_ecriture). */
    @Column(name = "schema_ecriture_id")
    private UUID schemaEcritureId;
}
