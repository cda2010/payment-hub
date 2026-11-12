package com.gateway.payment.orchestration.domain;

import com.gateway.payment.common.domain.ServiceTypeCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Relation inter-FSE pour un type de service (aligné Omnicanal {@code Relation}).
 */
@Entity
@Table(
        name = "relation_fse",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_relation_fse_corridor",
                columnNames = {"fse_source", "fse_destination", "service_type"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelationFse {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "fse_source", nullable = false, length = 20)
    private String fseSource;

    @Column(name = "fse_destination", nullable = false, length = 20)
    private String fseDestination;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", length = 40)
    private ServiceTypeCode serviceType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
