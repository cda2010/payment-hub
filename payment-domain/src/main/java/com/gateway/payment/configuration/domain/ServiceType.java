package com.gateway.payment.configuration.domain;

import com.gateway.payment.common.domain.ServiceTypeCategory;
import com.gateway.payment.common.domain.ServiceTypeCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "service_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceType {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "code", length = 40, nullable = false)
    private ServiceTypeCode code;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ServiceTypeCategory category;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

    @Column(name = "ordre_affichage")
    private Integer ordreAffichage;
}
