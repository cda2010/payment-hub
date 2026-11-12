package com.gateway.payment.orchestration.domain;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderApi;
import com.gateway.payment.configuration.domain.ProviderOperation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Interface utilisateur : résolution (codeFse, typeMethode, typeEnvoi) → opération + endpoint HTTP.
 * {@code parametres} = constitutés Omnicanal ({@link ParametreUtilisateur}).
 */
@Entity
@Table(
        name = "interface_utilisateur",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_interface_utilisateur",
                columnNames = {"code_fse", "service_type", "type_methode", "type_envoi"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterfaceUtilisateur {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private PaymentProvider provider;

    @Column(name = "code_fse", nullable = false, length = 20)
    private String codeFse;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 40)
    private ServiceTypeCode serviceType;

    @Column(name = "type_methode", nullable = false, length = 64)
    private String typeMethode;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_envoi", nullable = false, length = 20)
    @Builder.Default
    private TypeEnvoi typeEnvoi = TypeEnvoi.REQUEST;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_operation_id")
    private ProviderOperation providerOperation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_api_id")
    private ProviderApi providerApi;

    @Column(name = "code_interface", length = 50)
    private String codeInterface;

    @Column(name = "nom_interface", length = 150)
    private String nomInterface;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

    @OneToMany(mappedBy = "interfaceUtilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC, codeParametre ASC")
    @Builder.Default
    private List<ParametreUtilisateur> parametres = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (typeEnvoi == null) {
            typeEnvoi = TypeEnvoi.REQUEST;
        }
    }
}
