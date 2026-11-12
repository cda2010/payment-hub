package com.gateway.payment.commission.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "commission_formule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormuleCommission {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, length = 200)
    private String libelle;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TypeFormuleCommission type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private StatutFormuleCommission status = StatutFormuleCommission.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "formule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<FormuleCommissionParam> parametres = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
