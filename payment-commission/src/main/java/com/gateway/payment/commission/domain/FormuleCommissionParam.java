package com.gateway.payment.commission.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "commission_formule_param")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormuleCommissionParam {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formule_id", nullable = false)
    private FormuleCommission formule;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 200)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 32)
    private TypeDonnee dataType;

    @Column(nullable = false)
    @Builder.Default
    private boolean required = true;

    @Column(name = "default_value")
    private String defaultValue;
}
