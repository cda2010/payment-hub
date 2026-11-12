package com.gateway.payment.identity.enrolement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "enrolement")
@Getter
@Setter
@NoArgsConstructor
public class Enrolement {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 16)
    private String origine;

    @Column(name = "party_id")
    private UUID partyId;

    @Column(nullable = false, length = 30)
    private String telephone;

    @Column(name = "numero_saisi", length = 40)
    private String numeroSaisi;

    @Column(name = "workflow_code", nullable = false, length = 40)
    private String workflowCode = EnrolementWorkflow.NOM;

    @Column(nullable = false, length = 20)
    private String statut;

    @Column(name = "recto_document_id")
    private UUID rectoDocumentId;

    @Column(name = "verso_document_id")
    private UUID versoDocumentId;

    @Column(name = "selfie_document_id")
    private UUID selfieDocumentId;

    @Column(name = "code_hash", length = 64)
    private String codeHash;

    @Column(name = "code_expire_at")
    private Instant codeExpireAt;

    @Column(nullable = false)
    private int essais;

    @Column(length = 80)
    private String nom;

    @Column(length = 80)
    private String prenom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(length = 8)
    private String pays;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> complements;

    @Column(name = "numero_ouvert", length = 40)
    private String numeroOuvert;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Présent seulement le temps de la réponse, quand le profil local l'autorise. */
    @Transient
    private String codeClair;
}
