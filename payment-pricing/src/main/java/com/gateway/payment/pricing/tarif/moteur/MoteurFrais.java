package com.gateway.payment.pricing.tarif.moteur;

import com.gateway.payment.pricing.tarif.TarifException;
import com.gateway.payment.pricing.tarif.domain.Formule;
import com.gateway.payment.pricing.tarif.domain.GrilleTarifaire;
import com.gateway.payment.pricing.tarif.domain.TrancheTarifaire;
import com.gateway.payment.pricing.tarif.formule.FeeFormula;
import com.gateway.payment.pricing.tarif.formule.FormulaRegistry;
import com.gateway.payment.pricing.tarif.formule.ResultatFormule;
import com.gateway.payment.pricing.tarif.moteur.ResultatFrais.Correspondance;
import com.gateway.payment.pricing.tarif.repository.GrilleTarifaireRepository;
import com.gateway.payment.pricing.tarif.repository.GrilleTarifaireRepository.Perimetre;
import com.gateway.payment.pricing.tarif.reseau.PositionReseau;
import com.gateway.payment.pricing.tarif.reseau.ReseauClient;
import com.gateway.payment.pricing.tarif.reseau.ReseauException;
import com.gateway.payment.pricing.tarif.validation.ParametreValidator;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Calcul des frais. Ordre de traitement :
 * <ol>
 *   <li>sélection de la grille publiée valide à la date pour produit + devise, dont le canal, la source et la
 *       destination sont vides ou correspondent à l'opération ; la plus précise l'emporte, la source d'abord, puis
 *       la destination, puis le canal (voir {@link Ciblage}) ;</li>
 *   <li>sélection de la tranche couvrant le montant, bornes incluses ; aucune ou plusieurs → erreur ;</li>
 *   <li>validation des paramètres de la tranche contre le schéma de la version de formule ;</li>
 *   <li>calcul par l'implémentation approuvée de la formule ;</li>
 *   <li>plancher puis plafond de la tranche ;</li>
 *   <li>arrondi de la grille (par défaut à l'unité, HALF_UP).</li>
 * </ol>
 * Aucun montant n'est écrit dans les logs (ENF-009).
 */
@Slf4j
@Service
public class MoteurFrais {

    public static final String PLANCHER = "PLANCHER";
    public static final String PLAFOND = "PLAFOND";

    private final GrilleTarifaireRepository grilles;
    private final ParametreValidator parametreValidator;
    private final FormulaRegistry registry;
    private final ReseauClient reseau;
    private final Clock clock;
    private final MeterRegistry meters;

    public MoteurFrais(GrilleTarifaireRepository grilles, ParametreValidator parametreValidator,
                       FormulaRegistry registry, ReseauClient reseau, Clock clock, MeterRegistry meters) {
        this.grilles = grilles;
        this.parametreValidator = parametreValidator;
        this.registry = registry;
        this.reseau = reseau;
        this.clock = clock;
        this.meters = meters;
    }

    @Transactional(readOnly = true, noRollbackFor = TarifException.class)
    public ResultatFrais calculer(DemandeFrais demande) {
        Timer.Sample sample = Timer.start(meters);
        String issue = "OK";
        try {
            controlerMontant(demande.montant());
            String produit = code(demande.produitCode());
            String canal = code(demande.canalCode());
            String devise = code(demande.devise());
            if (produit == null || devise == null) {
                throw new TarifException(HttpStatus.BAD_REQUEST, "CONTEXTE_INVALIDE",
                        "Le produit et la devise sont obligatoires.");
            }
            LocalDate date = demande.dateCalcul() != null ? demande.dateCalcul() : LocalDate.now(clock);
            Retenue retenue = selectionnerGrille(demande, produit, canal, devise, date);
            AssietteFrais.Resolution assiette = AssietteFrais.resoudre(retenue.grille(), demande);
            return appliquer(retenue.grille(), assiette.montant(), date)
                    .avecCorrespondances(retenue.source(), retenue.destination())
                    .avecAssiette(assiette.baseCalcul(), assiette.period(), assiette.scope());
        } catch (TarifException e) {
            issue = e.getCode();
            throw e;
        } finally {
            sample.stop(meters.timer("pricing.frais.calcul", "issue", issue));
        }
    }

    /** Applique une grille donnée, quel que soit son statut : utilisé par le calcul et par la simulation. */
    @Transactional(readOnly = true, noRollbackFor = TarifException.class)
    public ResultatFrais appliquer(GrilleTarifaire grille, BigDecimal montant, LocalDate date) {
        controlerMontant(montant);
        TrancheTarifaire tranche = selectionnerTranche(grille, montant);
        Formule formule = tranche.getFormule();

        ParametreValidator.Resultat parametres =
                parametreValidator.valider(formule, tranche.getParametres(), tranche.getOrdre());
        if (!parametres.valide()) {
            throw TarifException.calcul("CONFIGURATION_INVALIDE",
                    "Paramètres invalides dans la tranche " + tranche.getOrdre() + " de " + grille.libelleVersion() + ".",
                    parametres.erreurs());
        }
        ResultatFormule brut = implementation(formule).calculer(montant, parametres.valeurs());
        Borne borne = borner(brut.frais(), tranche.getFraisMin(), tranche.getFraisMax());
        BigDecimal frais = borne.frais();
        String ajustement = borne.ajustement();
        BigDecimal arrondi = frais.setScale(grille.getArrondiEchelle(), grille.getArrondiMode());

        log.debug("Frais calculés : grille={} tranche={} formule={}",
                grille.libelleVersion(), tranche.getOrdre(), formule.libelleVersion());

        return new ResultatFrais(
                montant,
                grille.getDevise(),
                grille.getId(),
                grille.getCode(),
                grille.getVersion(),
                new ResultatFrais.TrancheRetenue(tranche.getOrdre(), court(tranche.getMontantMin()),
                        court(tranche.getMontantMax()), court(tranche.getFraisMin()), court(tranche.getFraisMax())),
                formule.getCode(),
                formule.getVersion(),
                parametres.valeurs().valeurs(),
                brut.baseTaxable(),
                brut.frais(),
                ajustement,
                frais,
                new ResultatFrais.Arrondi(grille.getArrondiEchelle(), grille.getArrondiMode()),
                arrondi,
                brut.detail(),
                date);
    }

    /**
     * Essai d'une formule avec des paramètres non enregistrés (saisie d'une tranche) : mêmes contrôles,
     * même calcul et mêmes plancher/plafond qu'une tranche ; l'arrondi de la grille n'est pas appliqué.
     */
    public EssaiFormule essayer(Formule formule, Map<String, Object> parametres, BigDecimal montant,
                                BigDecimal fraisMin, BigDecimal fraisMax) {
        controlerMontant(montant);
        ParametreValidator.Resultat valides = parametreValidator.valider(formule, parametres, null);
        if (!valides.valide()) {
            return EssaiFormule.invalide(valides.erreurs());
        }
        ResultatFormule brut = implementation(formule).calculer(montant, valides.valeurs());
        Borne borne = borner(brut.frais(), fraisMin, fraisMax);
        return new EssaiFormule(true, List.of(), valides.valeurs().valeurs(), brut.baseTaxable(), brut.frais(),
                borne.ajustement(), borne.frais(), brut.detail());
    }

    private FeeFormula implementation(Formule formule) {
        return registry.trouver(formule.getCode(), formule.getVersion())
                .orElseThrow(() -> TarifException.calcul("FORMULE_NON_IMPLEMENTEE",
                        "Aucune implémentation approuvée pour la formule " + formule.libelleVersion() + "."));
    }

    private record Borne(BigDecimal frais, String ajustement) {
    }

    /** Plancher puis plafond de la tranche, s'ils sont renseignés. */
    private static Borne borner(BigDecimal frais, BigDecimal fraisMin, BigDecimal fraisMax) {
        if (fraisMin != null && frais.compareTo(fraisMin) < 0) {
            return new Borne(fraisMin, PLANCHER);
        }
        if (fraisMax != null && frais.compareTo(fraisMax) > 0) {
            return new Borne(fraisMax, PLAFOND);
        }
        return new Borne(frais, null);
    }

    private record Retenue(GrilleTarifaire grille, Correspondance source, Correspondance destination) {
    }

    /** Grille candidate et ses rangs de précision (source, destination, canal) : le plus petit l'emporte. */
    private record Classee(GrilleTarifaire grille, Ciblage.Rang source, Ciblage.Rang destination, int canal) {
        static final Comparator<Classee> PRECISION = Comparator
                .comparingInt((Classee c) -> c.source().valeur())
                .thenComparingInt(c -> c.destination().valeur())
                .thenComparingInt(Classee::canal);
    }

    /**
     * Grille qui s'appliquerait à une source et une destination à la date donnée, sans montant : sert, pendant la
     * saisie d'une grille, à montrer le tarif en place et ce que la nouvelle grille remplacerait. Contrairement au
     * calcul, un côté peut être un <strong>regroupement</strong> : on lit alors la cible telle qu'elle est stockée
     * (unité de type {@code REGROUPEMENT} dans l'arbre), pour détecter une grille déjà publiée sur ce même
     * regroupement ou une grille moins précise qu'elle masquerait. Les positions sont toujours lues, pour compter
     * les grilles qui visent déjà des unités sœurs.
     */
    @Transactional(readOnly = true, noRollbackFor = TarifException.class)
    public GrilleApplicable applicable(DemandeFrais demande) {
        String produit = code(demande.produitCode());
        String canal = code(demande.canalCode());
        String devise = code(demande.devise());
        if (produit == null || devise == null) {
            throw new TarifException(HttpStatus.BAD_REQUEST, "CONTEXTE_INVALIDE",
                    "Le produit et la devise sont obligatoires.");
        }
        LocalDate date = demande.dateCalcul() != null ? demande.dateCalcul() : LocalDate.now(clock);
        PositionReseau source = position(demande.source(), "source", true);
        PositionReseau destination = position(demande.destination(), "destination", true);
        List<Classee> classees = classer(produit, canal, devise, date, source, destination);
        long voisinesSource = voisines(source, p -> grilles.compterVoisinesSource(produit, devise, date,
                parent(p), p.unite().type(), p.unite().id()));
        long voisinesDestination = voisines(destination, p -> grilles.compterVoisinesDestination(produit, devise, date,
                parent(p), p.unite().type(), p.unite().id()));
        if (classees.isEmpty()) {
            return new GrilleApplicable(GrilleApplicable.Statut.AUCUNE, "Aucune grille publiée ne s'applique à "
                    + produit + " / " + (canal == null ? "tous canaux" : canal) + " / " + devise + " pour cette source et "
                    + "cette destination au " + date + ".", null, List.of(), voisinesSource, voisinesDestination);
        }
        Classee meilleure = classees.get(0);
        List<Classee> exaequo = exaequo(classees);
        if (exaequo.size() > 1) {
            return new GrilleApplicable(GrilleApplicable.Statut.AMBIGUE, "Plusieurs grilles publiées s'appliquent "
                    + "avec la même précision : " + libelles(exaequo) + ".", null,
                    exaequo.stream().map(MoteurFrais::retenue).toList(), voisinesSource, voisinesDestination);
        }
        return new GrilleApplicable(GrilleApplicable.Statut.TROUVEE, null, retenue(meilleure),
                classees.stream().skip(1).limit(5).map(MoteurFrais::retenue).toList(),
                voisinesSource, voisinesDestination);
    }

    /**
     * La position d'un côté n'est demandée au réseau que si ce côté est renseigné et qu'au moins une grille
     * applicable le cible ; seules les grilles qui peuvent viser l'opération sont ensuite lues en base.
     */
    private Retenue selectionnerGrille(DemandeFrais demande, String produit, String canal, String devise, LocalDate date) {
        PositionReseau source = grilles.compterCibleesSource(produit, canal, devise, date) > 0
                ? position(demande.source(), "source", false) : null;
        PositionReseau destination = grilles.compterCibleesDestination(produit, canal, devise, date) > 0
                ? position(demande.destination(), "destination", false) : null;
        List<Classee> classees = classer(produit, canal, devise, date, source, destination);
        if (classees.isEmpty()) {
            String perimetre = produit + " / " + (canal == null ? "tous canaux" : canal) + " / " + devise + " au " + date;
            if (grilles.compterApplicables(produit, canal, devise, date) == 0) {
                throw TarifException.calcul("AUCUNE_GRILLE_APPLICABLE", "Aucune grille publiée pour " + perimetre + ".");
            }
            throw TarifException.calcul("AUCUNE_GRILLE_APPLICABLE", "Aucune grille publiée pour " + perimetre
                    + " ne vise cette source (" + demande.source().texte() + ") et cette destination ("
                    + demande.destination().texte() + ").");
        }
        List<Classee> exaequo = exaequo(classees);
        if (exaequo.size() > 1) {
            throw TarifException.calcul("GRILLE_AMBIGUE",
                    "Plusieurs grilles publiées s'appliquent avec la même précision : " + libelles(exaequo) + ".");
        }
        Classee meilleure = classees.get(0);
        return new Retenue(meilleure.grille(), meilleure.source().correspondance(),
                meilleure.destination().correspondance());
    }

    private List<Classee> classer(String produit, String canal, String devise, LocalDate date,
                                  PositionReseau source, PositionReseau destination) {
        return grilles.candidates(produit, canal, devise, date, Perimetre.de(source), Perimetre.de(destination))
                .stream()
                .map(g -> classer(g, source, destination))
                .flatMap(Optional::stream)
                .sorted(Classee.PRECISION)
                .toList();
    }

    private static Optional<Classee> classer(GrilleTarifaire g, PositionReseau source, PositionReseau destination) {
        Optional<Ciblage.Rang> s = Ciblage.rang(g.getSource(), source);
        Optional<Ciblage.Rang> d = Ciblage.rang(g.getDestination(), destination);
        if (s.isEmpty() || d.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Classee(g, s.get(), d.get(), g.getCanal() != null ? 0 : 1));
    }

    private static List<Classee> exaequo(List<Classee> classees) {
        Classee meilleure = classees.get(0);
        return classees.stream().filter(c -> Classee.PRECISION.compare(c, meilleure) == 0).toList();
    }

    private static String libelles(List<Classee> classees) {
        return classees.stream().map(c -> c.grille().libelleVersion()).sorted().collect(Collectors.joining(", "));
    }

    private static GrilleApplicable.Retenue retenue(Classee c) {
        GrilleTarifaire g = c.grille();
        return new GrilleApplicable.Retenue(g.getId(), g.getCode(), g.getVersion(), g.getLibelle(), g.getCanal(),
                g.getDateDebut(), g.getDateFin(), c.source().correspondance(), c.destination().correspondance());
    }

    /** 0 si le côté n'est pas une unité rattachée (pays, zone, côté non renseigné). */
    private static long voisines(PositionReseau p, Function<PositionReseau, Long> compter) {
        return p == null || p.unite() == null || p.lignee().size() < 2 ? 0 : compter.apply(p);
    }

    private static UUID parent(PositionReseau p) {
        return p.lignee().get(1).id();
    }

    /**
     * Position d'un côté dans le réseau ; vide si ce côté n'est pas renseigné.
     *
     * @param autoriserRegroupement {@code true} pour le tarif actuel d'une cible de grille (un regroupement est une
     *                              unité d'organisation qu'on peut viser) ; {@code false} pour le calcul d'une
     *                              transaction (l'opération a lieu sur une unité opérationnelle, pas sur un
     *                              regroupement)
     */
    private PositionReseau position(Localisation loc, String cote, boolean autoriserRegroupement) {
        if (loc.estVide()) {
            return null;
        }
        PositionReseau position;
        try {
            if (loc.uniteId() != null) {
                position = reseau.unite(loc.uniteId());
            } else if (loc.pays() != null && !loc.pays().isBlank()) {
                position = reseau.pays(code(loc.pays()));
            } else {
                position = reseau.zone(code(loc.zone()));
            }
        } catch (ReseauException e) {
            if (e.isIntrouvable()) {
                throw new TarifException(HttpStatus.BAD_REQUEST, "CONTEXTE_INVALIDE",
                        "Côté " + cote + " : " + e.getMessage());
            }
            throw new TarifException(HttpStatus.SERVICE_UNAVAILABLE, "RESEAU_INDISPONIBLE", e.getMessage());
        }
        if (!autoriserRegroupement && position.unite() != null && position.unite().estRegroupement()) {
            throw new TarifException(HttpStatus.BAD_REQUEST, "CONTEXTE_INVALIDE", "Côté " + cote + " : "
                    + position.unite().code() + " est un regroupement ; indiquez l'unité où se passe l'opération.");
        }
        return position;
    }

    private static TrancheTarifaire selectionnerTranche(GrilleTarifaire grille, BigDecimal montant) {
        List<TrancheTarifaire> couvrantes = grille.getTranches().stream().filter(t -> t.couvre(montant)).toList();
        if (couvrantes.isEmpty()) {
            throw TarifException.calcul("AUCUNE_TRANCHE",
                    "Aucune tranche de " + grille.libelleVersion() + " ne couvre ce montant.");
        }
        if (couvrantes.size() > 1) {
            throw TarifException.calcul("TRANCHES_CHEVAUCHEMENT",
                    "Plusieurs tranches de " + grille.libelleVersion() + " couvrent ce montant : "
                            + couvrantes.stream().map(t -> String.valueOf(t.getOrdre())).collect(Collectors.joining(", "))
                            + ".");
        }
        return couvrantes.get(0);
    }

    private static void controlerMontant(BigDecimal montant) {
        if (montant == null || montant.signum() < 0) {
            throw new TarifException(HttpStatus.BAD_REQUEST, "MONTANT_INVALIDE",
                    "Le montant est obligatoire et doit être positif ou nul.");
        }
    }

    /** NUMERIC(18,2) relit 8001 en 8001.00 : on renvoie la valeur sans zéros inutiles. */
    private static BigDecimal court(BigDecimal v) {
        if (v == null) {
            return null;
        }
        BigDecimal s = v.stripTrailingZeros();
        return s.scale() < 0 ? s.setScale(0) : s;
    }

    static String code(String valeur) {
        return valeur == null || valeur.isBlank() ? null : valeur.trim().toUpperCase(Locale.ROOT);
    }
}
