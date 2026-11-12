package com.gateway.payment.orchestration.domain;

import com.gateway.payment.common.domain.ServiceTypeCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Schéma d'écriture applicatif — pilote le workflow BPMN et le matching par {@code patternCode}.
 */
@Entity
@Table(
        name = "schema_ecriture",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_schema_ecriture_service_pattern",
                columnNames = {"service_type", "pattern_code"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchemaEcriture {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "nom_schema", nullable = false, length = 150)
    private String nomSchema;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 40)
    private ServiceTypeCode serviceType;

    @Column(name = "pattern_code", nullable = false, length = 64)
    private String patternCode;

    @Column(name = "workflow_name", length = 128)
    private String workflowName;

    @Column(name = "workflow_version", nullable = false)
    @Builder.Default
    private Integer workflowVersion = 1;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String engine = "flowable";

    @Column(name = "error_policy_code", length = 64)
    private String errorPolicyCode;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "schemaEcriture", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC")
    @Builder.Default
    private List<LigneSchemaEcriture> lignes = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (workflowVersion == null) {
            workflowVersion = 1;
        }
        if (engine == null || engine.isBlank()) {
            engine = "flowable";
        }
    }
}
