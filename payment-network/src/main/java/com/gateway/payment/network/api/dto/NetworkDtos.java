package com.gateway.payment.network.api.dto;

import com.gateway.payment.network.domain.FamilleFse;
import com.gateway.payment.network.domain.StatutUnite;
import com.gateway.payment.network.domain.TypeUnite;
import com.gateway.payment.network.domain.UniteOrganisation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class NetworkDtos {

    private NetworkDtos() {
    }

    // ── Unités ──────────────────────────────────────────────────────────────

    public record UniteResume(
            UUID id, String code, String libelle, TypeUnite type, String zone, String codeFse, FamilleFse famille,
            StatutUnite statut, int niveau, UUID parentId
    ) {
        public static UniteResume from(UniteOrganisation u) {
            return u == null ? null : new UniteResume(u.getId(), u.getCode(), u.getLibelle(), u.getType(), u.getZone(),
                    u.getCodeFse(), u.getFamilleFse(), u.getStatut(), u.getNiveau(), u.getParentId());
        }
    }

    /**
     * @param codeFse               code FSE Omnicanal si {@code type = FSE} ; sinon null
     * @param ascendants            du pays au parent direct ; vide pour un pays
     * @param typesEnfantsAutorises types qu'on peut rattacher sous cette unité
     * @param nbMembres             regroupement seulement : membres directs (leur descendance est couverte aussi)
     */
    public record UniteDetail(
            UUID id, String code, String libelle, TypeUnite type, String zone, String codeFse, String devise,
            StatutUnite statut, int niveau, UniteResume parent, List<UniteResume> ascendants, long nbEnfants,
            long nbMembres, List<TypeUnite> typesEnfantsAutorises,
            Instant createdAt, String createdBy, Instant updatedAt, String updatedBy
    ) {
    }

    /**
     * Nœud de l'arbre, chargé à la demande : ses enfants (ou, pour un regroupement, ses membres) se lisent à part.
     *
     * @param nbEnfants unités rattachées directement, regroupements compris
     * @param nbMembres regroupement seulement : membres directs
     */
    public record NoeudArbre(
            UUID id, String code, String libelle, TypeUnite type, String zone, StatutUnite statut, int niveau,
            UUID parentId, long nbEnfants, long nbMembres
    ) {
        public static NoeudArbre from(UniteOrganisation u, long nbEnfants, long nbMembres) {
            return new NoeudArbre(u.getId(), u.getCode(), u.getLibelle(), u.getType(), u.getZone(), u.getStatut(),
                    u.getNiveau(), u.getParentId(), nbEnfants, nbMembres);
        }
    }

    /** Page de résultats : `page` commence à 0, `total` compte les résultats de toutes les pages. */
    public record PageDto<T>(List<T> contenu, long total, int page, int taille) {
        public static <T> PageDto<T> of(Page<T> p) {
            return new PageDto<>(p.getContent(), p.getTotalElements(), p.getNumber(), p.getSize());
        }
    }

    /**
     * Création d'une unité. La zone (code) est obligatoire pour un pays ; ailleurs elle vient du parent et, si elle
     * est fournie, doit lui être identique. Le code d'un pays est son code ISO 3166 alpha-2.
     * {@code codeFse} est obligatoire si {@code type = FSE} (code Omnicanal, ex. 0011) ; interdit sinon.
     */
    public record CreationUniteRequest(
            @NotBlank @Pattern(regexp = "(?i)[A-Z0-9_-]{1,40}", message = "lettres, chiffres, _ ou - (40 max)") String code,
            @NotBlank @Size(max = 150) String libelle,
            @NotNull TypeUnite type,
            String zone,
            UUID parentId,
            @Size(max = 20) String codeFse
    ) {
    }

    /** Le code, la zone et le parent ne changent pas ici (le parent change par un déplacement). */
    public record ModificationUniteRequest(
            @NotBlank @Size(max = 150) String libelle,
            @NotNull TypeUnite type
    ) {
    }

    public record DeplacementRequest(@NotNull UUID parentId) {
    }

    // ── Regroupements ───────────────────────────────────────────────────────

    public record MembreRequest(@NotNull UUID uniteId) {
    }

    public record MembreDto(UniteResume unite, Instant ajouteLe, String ajoutePar) {
    }

    /**
     * Regroupement qui couvre une unité.
     *
     * @param via    membre du regroupement par lequel l'unité est couverte (elle-même ou un ascendant)
     * @param direct vrai si l'unité est elle-même membre
     */
    public record CouvertureDto(UniteResume regroupement, UniteResume via, boolean direct) {
    }

    /**
     * Position d'une unité, d'un pays ou d'une zone dans le réseau, pour les services qui ciblent des unités
     * (ex. les grilles tarifaires).
     *
     * @param unite          vide pour une zone
     * @param lignee         l'unité puis ses ascendants jusqu'au pays (du plus précis au moins précis) ; vide pour une zone
     * @param regroupements  regroupements qui couvrent l'unité, avec le membre par lequel ils la couvrent
     */
    public record PositionDto(String zone, String devise, UniteResume unite, List<UniteResume> lignee,
                              List<CouvertureDto> regroupements) {
    }

    // ── Import en masse ─────────────────────────────────────────────────────

    /**
     * Résultat d'un import. En essai, ou dès qu'une ligne est en erreur, rien n'est enregistré (`applique` faux).
     *
     * @param nbCreations   unités créées, ou membres ajoutés
     * @param nbMisesAJour  unités existantes dont le libellé change
     * @param nbDeplacements unités existantes qui changent de parent (avec leur branche)
     * @param nbInchangees  lignes déjà conformes à la base
     * @param erreurs       les 500 premières (`nbErreurs` donne le total)
     */
    public record RapportImport(
            String objet, String format, boolean essai, boolean applique, int nbLignes, int nbCreations,
            int nbMisesAJour, int nbDeplacements, int nbInchangees, int nbErreurs, List<ErreurImport> erreurs,
            long dureeMs
    ) {
    }

    /** @param ligne numéro de ligne dans le fichier (l'en-tête est la ligne 1) ; 0 pour une erreur générale */
    public record ErreurImport(int ligne, String code, String champ, String codeErreur, String message) {
    }

    // ── Référentiel ─────────────────────────────────────────────────────────

    public record Referentiel(List<ZoneDto> zones, List<TypeDto> types) {
    }

    // ── Zones ───────────────────────────────────────────────────────────────

    /**
     * @param nbPays   pays de la zone, actifs ou non
     * @param nbUnites unités de la zone, tous types confondus ; la devise est figée dès qu'il y en a une
     */
    public record ZoneDto(
            String code, String libelle, String devise, StatutUnite statut, long nbPays, long nbUnites,
            Instant createdAt, String createdBy, Instant updatedAt, String updatedBy
    ) {
    }

    public record CreationZoneRequest(
            @NotBlank @Pattern(regexp = "(?i)[A-Z][A-Z0-9_]{1,9}", message = "2 à 10 lettres, chiffres ou _, commençant par une lettre")
            String code,
            @NotBlank @Size(max = 100) String libelle,
            @NotBlank @Pattern(regexp = "(?i)[A-Z]{3}", message = "code ISO 4217 sur 3 lettres (ex. XOF)") String devise
    ) {
    }

    /** Le code ne change pas ; la devise seulement tant que la zone ne contient aucune unité. */
    public record ModificationZoneRequest(
            @NotBlank @Size(max = 100) String libelle,
            @NotBlank @Pattern(regexp = "(?i)[A-Z]{3}", message = "code ISO 4217 sur 3 lettres (ex. XOF)") String devise
    ) {
    }

    /** @param regroupement vrai pour le type qui a des membres au lieu d'enfants */
    public record TypeDto(
            TypeUnite code, String libelle, boolean racine, boolean regroupement,
            Set<TypeUnite> parentsAutorises, List<TypeUnite> enfantsAutorises, String regle
    ) {
    }
}
