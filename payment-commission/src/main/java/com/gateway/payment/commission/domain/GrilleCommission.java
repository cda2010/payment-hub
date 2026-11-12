package com.gateway.payment.commission.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "commission_grille")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrilleCommission {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false)
    @Builder.Default
    private int version = 1;

    @Column(nullable = false, length = 200)
    private String libelle;

    @Column(length = 500)
    private String description;

    @Column(name = "operation_type", nullable = false, length = 64)
    private String operationType;

    @Column(name = "product_code", length = 80)
    private String productCode;

    @Column(name = "channel_code", length = 40)
    private String channelCode;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "XOF";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private StatutGrilleCommission status = StatutGrilleCommission.BROUILLON;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "envelope_mode", nullable = false, length = 32)
    @Builder.Default
    private EnvelopeMode envelopeMode = EnvelopeMode.FULL_FEES;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "grille", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("priority ASC")
    @Builder.Default
    private List<RegleCommission> regles = new ArrayList<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (validFrom == null) {
            validFrom = LocalDate.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean estApplicable(LocalDate date) {
        if (status != StatutGrilleCommission.PUBLIEE) {
            return false;
        }
        if (date.isBefore(validFrom)) {
            return false;
        }
        return validTo == null || !date.isAfter(validTo);
    }
}
