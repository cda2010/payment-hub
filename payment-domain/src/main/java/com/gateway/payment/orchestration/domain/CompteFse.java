package com.gateway.payment.orchestration.domain;

import com.gateway.payment.common.domain.ServiceTypeCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
 * Compte catalogue — schéma aligné Omnicanal {@code compte} / {@code CompteFse}.
 * Les FK bigint Omnicanal (type_compte_id, fse_*_id, service_id, agence_id)
 * sont portées en codes métier payment-2.
 */
@Entity
@Table(name = "compte_fse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteFse {

    @Id
    @UuidGenerator
    private UUID id;

    /** Id source Omnicanal ({@code compte.id}), pour sync. */
    @Column(name = "omni_id")
    private Long omniId;

    @Column(name = "code_banque", nullable = false, length = 20)
    private String codeBanque;

    @Column(name = "code_agence", nullable = false, length = 20)
    private String codeAgence;

    @Column(name = "numero_compte", nullable = false, length = 20)
    private String numeroCompte;

    @Column(name = "cle", length = 5)
    private String cle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relation_id")
    private RelationFse relation;

    @Column(name = "extype", nullable = false, length = 20)
    @Builder.Default
    private String extype = "CompteFse";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_compte_code", nullable = false)
    private TypeCompte typeCompte;

    @Column(name = "devise", length = 10)
    private String devise;

    /** FSE qui héberge le compte (teneur) — Omnicanal {@code fse_teneur_id}. */
    @Column(name = "fse_teneur", nullable = false, length = 20)
    private String fseTeneur;

    /** FSE titulaire — Omnicanal {@code fse_titulaire_id}. */
    @Column(name = "fse_titulaire", nullable = false, length = 20)
    private String fseTitulaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", length = 40)
    private ServiceTypeCode serviceType;

    @Column(name = "libelle_compte", nullable = false, length = 200)
    @Builder.Default
    private String libelleCompte = "";

    @Column(name = "code_uo_titulaire", length = 20)
    private String codeUoTitulaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mirroir_id")
    private CompteFse mirroir;

    /** Code agence (Omnicanal {@code agence.code_agence}). */
    @Column(name = "agence_code", nullable = false, length = 20)
    @Builder.Default
    private String agenceCode = "01001";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (extype == null || extype.isBlank()) {
            extype = "CompteFse";
        }
        if (codeBanque == null) {
            codeBanque = "00000";
        }
        if (codeAgence == null) {
            codeAgence = "";
        }
        if (fseTitulaire == null || fseTitulaire.isBlank()) {
            fseTitulaire = fseTeneur;
        }
        if (libelleCompte == null) {
            libelleCompte = "";
        }
        if (agenceCode == null || agenceCode.isBlank()) {
            agenceCode = codeAgence != null && !codeAgence.isBlank() ? codeAgence : "01001";
        }
    }
}
