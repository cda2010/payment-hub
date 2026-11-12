package com.gateway.payment.pricing.tarif.api.dto;

import com.gateway.payment.pricing.tarif.domain.BaseCalculFrais;
import com.gateway.payment.pricing.tarif.domain.CibleReseau;
import com.gateway.payment.pricing.tarif.domain.Formule;
import com.gateway.payment.pricing.tarif.domain.FormuleParametre;
import com.gateway.payment.pricing.tarif.domain.GrilleTarifaire;
import com.gateway.payment.pricing.tarif.domain.PeriodeCumul;
import com.gateway.payment.pricing.tarif.domain.ScopeCumul;
import com.gateway.payment.pricing.tarif.domain.StatutFormule;
import com.gateway.payment.pricing.tarif.domain.StatutGrille;
import com.gateway.payment.pricing.tarif.domain.TrancheTarifaire;
import com.gateway.payment.pricing.tarif.domain.TypeDonnee;
import com.gateway.payment.pricing.tarif.domain.TypeFormule;
import com.gateway.payment.pricing.tarif.validation.ValidationIssue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TarifDtos {

    private TarifDtos() {
    }

    // ── Formules ────────────────────────────────────────────────────────────

    public record FormuleResume(
            UUID id, String code, String libelle, int version, TypeFormule type, StatutFormule statut,
            String expression, String description
    ) {
        public static FormuleResume from(Formule f) {
            return new FormuleResume(f.getId(), f.getCode(), f.getLibelle(), f.getVersion(), f.getType(),
                    f.getStatut(), f.getExpression(), f.getDescription());
        }
    }

    public record FormuleDetail(
            UUID id, String code, String libelle, int version, TypeFormule type, StatutFormule statut,
            String expression, String description, List<ParametreDto> parametres
    ) {
        public static FormuleDetail from(Formule f) {
            return new FormuleDetail(f.getId(), f.getCode(), f.getLibelle(), f.getVersion(), f.getType(),
                    f.getStatut(), f.getExpression(), f.getDescription(),
                    f.getParametres().stream().map(ParametreDto::from).toList());
        }
    }

    public record ParametreDto(
            String code, String libelle, TypeDonnee typeDonnee, boolean obligatoire, String valeurDefaut,
            BigDecimal valeurMin, BigDecimal valeurMax, Integer nbDecimales, String unite, int ordre, String aide,
            List<String> valeursAutorisees, Integer longueurMax, String format
    ) {
        public static ParametreDto from(FormuleParametre p) {
            return new ParametreDto(p.getCode(), p.getLibelle(), p.getTypeDonnee(), p.isObligatoire(),
                    p.getValeurDefaut(), plain(p.getValeurMin()), plain(p.getValeurMax()), p.getNbDecimales(),
                    p.getUnite(), p.getOrdre(), p.getAide(), p.getValeursAutorisees(), p.getLongueurMax(),
                    p.getFormat());
        }
    }

    public record FormuleRef(
            @NotBlank String code,
            @NotNull @Min(1) Integer version
    ) {
    }

    // ── Grilles ─────────────────────────────────────────────────────────────

    public record GrilleResume(
            UUID id, String code, String libelle, String produit, String canal, String devise,
            CibleDto source, CibleDto destination,
            LocalDate dateDebut, LocalDate dateFin, int version, StatutGrille statut, int nbTranches,
            Instant updatedAt, String updatedBy
    ) {
        public static GrilleResume from(GrilleTarifaire g) {
            return new GrilleResume(g.getId(), g.getCode(), g.getLibelle(), g.getProduit(), g.getCanal(),
                    g.getDevise(), CibleDto.from(g.getSource()), CibleDto.from(g.getDestination()),
                    g.getDateDebut(), g.getDateFin(), g.getVersion(), g.getStatut(),
                    g.getTranches().size(), g.getUpdatedAt(), g.getUpdatedBy());
        }
    }

    public record GrilleDetail(
            UUID id, String code, String libelle, String produit, String canal, String devise,
            CibleDto source, CibleDto destination,
            LocalDate dateDebut, LocalDate dateFin, int version, StatutGrille statut, boolean modifiable,
            boolean continuiteRequise, int arrondiEchelle, RoundingMode arrondiMode,
            BaseCalculFrais baseCalcul, PeriodeCumul cumulPeriod, ScopeCumul cumulScope,
            List<TrancheDto> tranches,
            Instant createdAt, String createdBy, Instant updatedAt, String updatedBy
    ) {
        public static GrilleDetail from(GrilleTarifaire g) {
            return new GrilleDetail(g.getId(), g.getCode(), g.getLibelle(), g.getProduit(), g.getCanal(),
                    g.getDevise(), CibleDto.from(g.getSource()), CibleDto.from(g.getDestination()),
                    g.getDateDebut(), g.getDateFin(), g.getVersion(), g.getStatut(),
                    g.estModifiable(), g.isContinuiteRequise(), g.getArrondiEchelle(), g.getArrondiMode(),
                    g.getBaseCalcul() != null ? g.getBaseCalcul() : BaseCalculFrais.TRANSACTION_AMOUNT,
                    g.getCumulPeriod(), g.getCumulScope(),
                    g.getTranches().stream().map(TrancheDto::from).toList(),
                    g.getCreatedAt(), g.getCreatedBy(), g.getUpdatedAt(), g.getUpdatedBy());
        }
    }

    /**
     * Source ou destination d'une grille.
     *
     * @param niveau TOUTES (rien de renseigné), ZONE ou UNITE (unité de n'importe quel type, regroupement compris)
     */
    public record CibleDto(
            CibleReseau.Niveau niveau, String zone, UUID uniteId, String uniteCode, String uniteLibelle, String uniteType
    ) {
        public static CibleDto from(CibleReseau c) {
            if (CibleReseau.estVide(c)) {
                return new CibleDto(CibleReseau.Niveau.TOUTES, null, null, null, null, null);
            }
            return new CibleDto(CibleReseau.niveau(c), c.getZone(), c.getUniteId(), c.getUniteCode(), c.getUniteLibelle(),
                    c.getUniteType());
        }
    }

    public record TrancheDto(
            UUID id, int ordre, BigDecimal montantMin, BigDecimal montantMax, FormuleRef formule,
            String formuleLibelle, Map<String, Object> parametres, BigDecimal fraisMin, BigDecimal fraisMax
    ) {
        public static TrancheDto from(TrancheTarifaire t) {
            return new TrancheDto(t.getId(), t.getOrdre(), plain(t.getMontantMin()), plain(t.getMontantMax()),
                    new FormuleRef(t.getFormule().getCode(), t.getFormule().getVersion()),
                    t.getFormule().getLibelle(), t.getParametres(), plain(t.getFraisMin()), plain(t.getFraisMax()));
        }
    }

    /**
     * Création ou modification d'un brouillon. En modification, {@code tranches} absent laisse les tranches
     * existantes ; renseigné (même vide), il les remplace toutes.
     */
    public record GrilleRequest(
            @NotBlank @Pattern(regexp = "(?i)[A-Z0-9_-]{1,64}", message = "lettres, chiffres, _ ou - (64 max)") String code,
            @NotBlank @Size(max = 150) String libelle,
            @NotBlank @Size(max = 80) String produit,
            @Size(max = 40) String canal,
            @NotBlank @Pattern(regexp = "(?i)[A-Z]{3}", message = "code ISO 4217 sur 3 lettres") String devise,
            @NotNull LocalDate dateDebut,
            LocalDate dateFin,
            Boolean continuiteRequise,
            @Min(0) @Max(6) Integer arrondiEchelle,
            RoundingMode arrondiMode,
            List<@Valid TrancheRequest> tranches,
            @Valid CibleRequest source,
            @Valid CibleRequest destination,
            BaseCalculFrais baseCalcul,
            PeriodeCumul cumulPeriod,
            ScopeCumul cumulScope
    ) {
        /** Compat tests / clients sans assiette. */
        public GrilleRequest(
                String code, String libelle, String produit, String canal, String devise,
                LocalDate dateDebut, LocalDate dateFin, Boolean continuiteRequise,
                Integer arrondiEchelle, RoundingMode arrondiMode, List<TrancheRequest> tranches,
                CibleRequest source, CibleRequest destination
        ) {
            this(code, libelle, produit, canal, devise, dateDebut, dateFin, continuiteRequise,
                    arrondiEchelle, arrondiMode, tranches, source, destination,
                    BaseCalculFrais.TRANSACTION_AMOUNT, null, null);
        }
    }

    /**
     * Cible d'une grille : vide = toutes ; zone seule = toute la zone ; unité = l'unité (de n'importe quel type) et
     * tout ce qu'elle couvre. Avec une unité, la zone est facultative et doit être la sienne.
     */
    public record CibleRequest(
            @Pattern(regexp = "(?i)[A-Z][A-Z0-9_]{1,9}", message = "code de zone (ex. UEMOA)") String zone,
            UUID uniteId
    ) {
    }

    public record TrancheRequest(
            @NotNull @PositiveOrZero BigDecimal montantMin,
            BigDecimal montantMax,
            @NotNull @Valid FormuleRef formule,
            Map<String, Object> parametres,
            BigDecimal fraisMin,
            BigDecimal fraisMax
    ) {
    }

    public record RapportValidation(boolean valide, List<ValidationIssue> erreurs) {
    }

    public record EssaiRequest(
            @NotNull @PositiveOrZero BigDecimal montant,
            Map<String, Object> parametres,
            BigDecimal fraisMin,
            BigDecimal fraisMax
    ) {
    }

    public record SimulationRequest(
            @NotNull @PositiveOrZero BigDecimal montant,
            LocalDate dateCalcul
    ) {
    }

    // ── Calcul ──────────────────────────────────────────────────────────────

    public record CalculRequest(
            @NotBlank String produitCode,
            String canalCode,
            @NotBlank String devise,
            @NotNull @PositiveOrZero BigDecimal montant,
            LocalDate dateCalcul,
            @Valid LocalisationDto source,
            @Valid LocalisationDto destination
    ) {
    }

    /** Grille en place pour une source et une destination (sans montant), pendant la saisie d'une grille. */
    public record ApplicableRequest(
            @NotBlank String produitCode,
            String canalCode,
            @NotBlank String devise,
            LocalDate dateCalcul,
            @Valid LocalisationDto source,
            @Valid LocalisationDto destination
    ) {
    }

    /** Page de résultats (page numérotée à partir de 0). */
    public record PageDto<T>(List<T> contenu, long total, int page, int taille) {
        public static <T> PageDto<T> of(org.springframework.data.domain.Page<T> p) {
            return new PageDto<>(p.getContent(), p.getTotalElements(), p.getNumber(), p.getSize());
        }
    }

    /**
     * Où se situe un côté de l'opération ; le critère le plus précis l'emporte (unité, puis pays, puis zone).
     * Tout vide : côté inconnu ou sans objet (ex. pas de destination).
     */
    public record LocalisationDto(
            UUID uniteId,
            @Pattern(regexp = "(?i)[A-Z]{2}", message = "code ISO du pays sur 2 lettres") String pays,
            @Pattern(regexp = "(?i)[A-Z][A-Z0-9_]{1,9}", message = "code de zone (ex. UEMOA)") String zone
    ) {
    }

    /** NUMERIC(18,2) relit 8000 en 8000.00 : on renvoie la forme saisie. */
    private static BigDecimal plain(BigDecimal v) {
        if (v == null) {
            return null;
        }
        BigDecimal s = v.stripTrailingZeros();
        return s.scale() < 0 ? s.setScale(0) : s;
    }
}
