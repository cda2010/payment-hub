package com.gateway.payment.network.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class GuichetDtos {

    private GuichetDtos() {
    }

    public record CreationUtilisateurRequest(
            @NotBlank String identifiant,
            @NotBlank String nom,
            @NotBlank String role,
            @NotBlank String canal,
            String telephone,
            String email
    ) {
    }

    /** Canal et destination utilisés quand le compte n'en a pas encore. */
    public record DestinationAccesRequest(String canal, String telephone, String email) {
    }

    public record UtilisateurDto(
            UUID id,
            UUID uniteId,
            String identifiant,
            String nom,
            String role,
            String statut,
            String telephone,
            String email,
            String canal,
            boolean nouveauCompte
    ) {
    }

    public record AffectationRequest(
            @NotNull UUID utilisateurId,
            @NotNull LocalDate debut,
            LocalDate fin
    ) {
    }

    public record DesaffectationRequest(@NotBlank String motif) {
    }

    public record AffectationDto(
            UUID id,
            UUID pointVenteId,
            String pointCode,
            String pointLibelle,
            UUID utilisateurId,
            String login,
            String nom,
            LocalDate debut,
            LocalDate fin,
            String motif,
            String statut,
            boolean effective
    ) {
    }

    public record RattachementDto(
            UUID utilisateurId,
            UUID uniteId,
            String codeUnite,
            String libelleUnite,
            String role,
            String statut,
            String audience
    ) {
    }

    public record GuichetMoiDto(
            UtilisateurDto utilisateur,
            UUID uniteId,
            String codeUnite,
            String libelleUnite,
            java.util.List<PointVenteDto> points,
            java.util.List<UtilisateurDto> caissiers,
            java.util.List<AffectationDto> affectations,
            java.util.List<RattachementDto> rattachements,
            UUID uniteActiveId,
            String avertissement
    ) {
    }

    public record CreationPointVenteRequest(
            @NotBlank String code,
            @NotBlank String libelle
    ) {
    }

    public record PointVenteDto(
            UUID id,
            UUID uniteId,
            String code,
            String libelle,
            String statut
    ) {
    }

    public record OuvertureCaisseRequest(@NotNull @DecimalMin("0.00") BigDecimal fondsDeclare) {
    }

    public record FermetureCaisseRequest(@NotNull @DecimalMin("0.00") BigDecimal fondsCompte) {
    }

    public record OperationCaisseRequest(
            @NotBlank String sens,
            @NotNull @DecimalMin("0.01") BigDecimal montant,
            String reference
    ) {
    }

    public record MotifCaisseRequest(@NotBlank String motif) {
    }

    public record CaisseDto(
            UUID id,
            UUID pointVenteId,
            String pointCode,
            String pointLibelle,
            UUID utilisateurId,
            String login,
            String nom,
            String statut,
            int numero,
            Instant ouverteLe,
            Instant fermeeLe,
            BigDecimal fondsAttendu,
            BigDecimal fondsDeclare,
            BigDecimal ecartOuverture,
            BigDecimal soldeCompteOuverture,
            BigDecimal depots,
            BigDecimal retraits,
            BigDecimal physique,
            BigDecimal soldeCompte,
            BigDecimal fondsPhysiqueCloture,
            BigDecimal ecartFermeture,
            BigDecimal soldeCompteFermeture,
            String motif
    ) {
    }

    public record PointCaisseDto(
            UUID id,
            String code,
            String libelle,
            BigDecimal fondsAttendu,
            BigDecimal soldeCompte,
            boolean disponible,
            String message
    ) {
    }

    public record SituationCaisseDto(
            boolean caissier,
            boolean peutOperer,
            UUID utilisateurId,
            String login,
            String nom,
            String role,
            String codeUnite,
            String libelleUnite,
            CaisseDto session,
            List<CaisseDto> aValider,
            List<PointCaisseDto> points
    ) {
    }

    public record MouvementRequest(
            @NotNull String sens,
            @NotNull UUID aliasSourceId,
            @NotNull UUID aliasCibleId,
            @NotNull @DecimalMin("0.01") BigDecimal montant,
            String devise,
            @NotNull UUID utilisateurId
    ) {
    }

    public record MouvementDto(
            UUID id,
            UUID uniteId,
            UUID pointVenteId,
            UUID utilisateurId,
            String sens,
            UUID aliasSourceId,
            UUID aliasCibleId,
            String etiquetteSource,
            String etiquetteCible,
            BigDecimal montant,
            String devise
    ) {
    }
}
