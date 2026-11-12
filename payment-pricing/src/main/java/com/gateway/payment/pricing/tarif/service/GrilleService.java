package com.gateway.payment.pricing.tarif.service;

import com.gateway.payment.pricing.tarif.TarifException;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.CibleRequest;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.FormuleRef;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.GrilleDetail;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.GrilleRequest;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.GrilleResume;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.PageDto;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.RapportValidation;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.SimulationRequest;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.TrancheRequest;
import com.gateway.payment.pricing.tarif.domain.CibleReseau;
import com.gateway.payment.pricing.tarif.domain.Formule;
import com.gateway.payment.pricing.tarif.domain.GrilleTarifaire;
import com.gateway.payment.pricing.tarif.domain.StatutGrille;
import com.gateway.payment.pricing.tarif.domain.TrancheTarifaire;
import com.gateway.payment.pricing.tarif.moteur.AssietteFrais;
import com.gateway.payment.pricing.tarif.moteur.MoteurFrais;
import com.gateway.payment.pricing.tarif.moteur.ResultatFrais;
import com.gateway.payment.pricing.tarif.repository.FormuleRepository;
import com.gateway.payment.pricing.tarif.repository.GrilleTarifaireRepository;
import com.gateway.payment.pricing.tarif.reseau.PositionReseau;
import com.gateway.payment.pricing.tarif.reseau.ReseauClient;
import com.gateway.payment.pricing.tarif.reseau.ReseauException;
import com.gateway.payment.pricing.tarif.validation.GrilleValidator;
import com.gateway.payment.pricing.tarif.validation.ValidationIssue;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Administration des grilles : consultation, brouillons et leurs tranches, contrôle de cohérence, simulation,
 * nouvelle version d'une grille figée et publication. Seul un brouillon est modifiable (TA-012) ; le circuit
 * de validation à quatre yeux (A_VALIDER, VALIDEE) vient en phase 2.
 * À l'enregistrement, chaque tranche est contrôlée seule (bornes, frais, paramètres) ; les contrôles entre
 * tranches (chevauchements, trous) sont rapportés par {@link #valider(UUID)} pour permettre une saisie progressive.
 * La source et la destination visées (zone ou unité du réseau) sont vérifiées auprès de payment-network : unité
 * existante et active, zone de l'unité, devise de la zone source égale à celle de la grille.
 */
@Service
@Transactional
public class GrilleService {

    private final GrilleTarifaireRepository grilles;
    private final FormuleRepository formules;
    private final GrilleValidator validator;
    private final MoteurFrais moteur;
    private final ReseauClient reseau;
    private final Clock clock;

    public GrilleService(GrilleTarifaireRepository grilles, FormuleRepository formules, GrilleValidator validator,
                         MoteurFrais moteur, ReseauClient reseau, Clock clock) {
        this.grilles = grilles;
        this.formules = formules;
        this.validator = validator;
        this.moteur = moteur;
        this.reseau = reseau;
        this.clock = clock;
    }

    /**
     * @param source      zone ou code d'unité visé en source (égalité exacte)
     * @param destination idem pour la destination
     */
    public record Filtre(String code, String libelle, String produit, String canal, String devise,
                         StatutGrille statut, LocalDate date, String source, String destination) {
    }

    /** Tri par code puis version décroissante ; 50 grilles par page par défaut, 200 au plus. */
    @Transactional(readOnly = true)
    public PageDto<GrilleResume> rechercher(Filtre filtre, Integer page, Integer taille) {
        Sort tri = Sort.by("code").ascending().and(Sort.by("version").descending());
        int t = taille == null || taille < 1 ? 50 : Math.min(taille, 200);
        PageRequest pr = PageRequest.of(page == null || page < 0 ? 0 : page, t, tri);
        return PageDto.of(grilles.findAll(specification(filtre), pr).map(GrilleResume::from));
    }

    @Transactional(readOnly = true)
    public GrilleDetail consulter(UUID id) {
        return GrilleDetail.from(charger(id));
    }

    public GrilleDetail creer(GrilleRequest req, String acteur) {
        String code = code(req.code());
        if (grilles.existsByCode(code)) {
            throw TarifException.conflit("GRILLE_CODE_EXISTANT",
                    "Une grille " + code + " existe déjà ; ouvrez-la et créez-en une nouvelle version pour la modifier.");
        }
        GrilleTarifaire grille = GrilleTarifaire.builder()
                .code(code)
                .version(1)
                .statut(StatutGrille.BROUILLON)
                .createdBy(acteur)
                .updatedBy(acteur)
                .build();
        appliquerEntete(grille, req);
        if (req.tranches() != null) {
            req.tranches().forEach(t -> grille.ajouterTranche(construire(t)));
        }
        verifierTranches(grille, grille.getTranches());
        grilles.save(grille);
        return enregistrer(grille);
    }

    public GrilleDetail modifier(UUID id, GrilleRequest req, String acteur) {
        GrilleTarifaire grille = chargerModifiable(id);
        String code = code(req.code());
        if (!code.equals(grille.getCode()) && grille.getVersion() > 1) {
            throw TarifException.conflit("GRILLE_CODE_FIGE",
                    "Le code d'une grille ne change pas d'une version à l'autre (" + grille.getCode() + ").");
        }
        if (!code.equals(grille.getCode()) && grilles.existsByCode(code)) {
            throw TarifException.conflit("GRILLE_CODE_EXISTANT", "Une grille " + code + " existe déjà.");
        }
        grille.setCode(code);
        appliquerEntete(grille, req);
        if (req.tranches() != null) {
            grille.getTranches().clear();
            req.tranches().forEach(t -> grille.ajouterTranche(construire(t)));
        }
        grille.setUpdatedBy(acteur);
        verifierTranches(grille, grille.getTranches());
        return enregistrer(grille);
    }

    public GrilleDetail ajouterTranche(UUID id, TrancheRequest req, String acteur) {
        GrilleTarifaire grille = chargerModifiable(id);
        TrancheTarifaire tranche = construire(req);
        grille.ajouterTranche(tranche);
        grille.setUpdatedBy(acteur);
        verifierTranches(grille, List.of(tranche));
        return enregistrer(grille);
    }

    public GrilleDetail modifierTranche(UUID id, UUID trancheId, TrancheRequest req, String acteur) {
        GrilleTarifaire grille = chargerModifiable(id);
        TrancheTarifaire tranche = tranche(grille, trancheId);
        TrancheTarifaire saisie = construire(req);
        tranche.setMontantMin(saisie.getMontantMin());
        tranche.setMontantMax(saisie.getMontantMax());
        tranche.setFormule(saisie.getFormule());
        tranche.setParametres(saisie.getParametres());
        tranche.setFraisMin(saisie.getFraisMin());
        tranche.setFraisMax(saisie.getFraisMax());
        grille.setUpdatedBy(acteur);
        verifierTranches(grille, List.of(tranche));
        return enregistrer(grille);
    }

    public GrilleDetail supprimerTranche(UUID id, UUID trancheId, String acteur) {
        GrilleTarifaire grille = chargerModifiable(id);
        grille.getTranches().remove(tranche(grille, trancheId));
        grille.setUpdatedBy(acteur);
        renumeroter(grille);
        return enregistrer(grille);
    }

    /** Copie une tranche (mêmes bornes, formule et paramètres) pour servir de base à la suivante. */
    public GrilleDetail dupliquerTranche(UUID id, UUID trancheId, String acteur) {
        GrilleTarifaire grille = chargerModifiable(id);
        grille.ajouterTranche(copier(tranche(grille, trancheId)));
        grille.setUpdatedBy(acteur);
        renumeroter(grille);
        return enregistrer(grille);
    }

    /**
     * Prépare la modification d'une grille figée (publiée, désactivée…) : copie complète en brouillon v+1.
     * Un brouillon déjà ouvert pour ce code est renvoyé tel quel : une seule version en préparation à la fois.
     */
    public GrilleDetail nouvelleVersion(UUID id, String acteur) {
        GrilleTarifaire source = charger(id);
        List<GrilleTarifaire> brouillons = grilles.findByCodeAndStatut(source.getCode(), StatutGrille.BROUILLON);
        if (!brouillons.isEmpty()) {
            return GrilleDetail.from(brouillons.get(0));
        }
        int version = grilles.findFirstByCodeOrderByVersionDesc(source.getCode())
                .map(GrilleTarifaire::getVersion)
                .orElse(source.getVersion()) + 1;
        GrilleTarifaire copie = GrilleTarifaire.builder()
                .code(source.getCode())
                .libelle(source.getLibelle())
                .produit(source.getProduit())
                .canal(source.getCanal())
                .devise(source.getDevise())
                .source(CibleReseau.copie(source.getSource()))
                .destination(CibleReseau.copie(source.getDestination()))
                .dateDebut(source.getDateDebut())
                .dateFin(source.getDateFin())
                .version(version)
                .statut(StatutGrille.BROUILLON)
                .continuiteRequise(source.isContinuiteRequise())
                .arrondiEchelle(source.getArrondiEchelle())
                .arrondiMode(source.getArrondiMode())
                .baseCalcul(source.getBaseCalcul() != null
                        ? source.getBaseCalcul()
                        : com.gateway.payment.pricing.tarif.domain.BaseCalculFrais.TRANSACTION_AMOUNT)
                .cumulPeriod(source.getCumulPeriod())
                .cumulScope(source.getCumulScope())
                .createdBy(acteur)
                .updatedBy(acteur)
                .build();
        source.getTranches().forEach(t -> copie.ajouterTranche(copier(t)));
        grilles.save(copie);
        return enregistrer(copie);
    }

    /**
     * Met un brouillon en service. Il doit passer tous les contrôles de cohérence, viser des unités toujours actives
     * et ne pas recouvrir une grille publiée d'un autre code sur le même périmètre (produit, canal, devise, source,
     * destination) (le moteur répondrait GRILLE_AMBIGUE). Les versions publiées
     * du même code qui ont commencé avant lui s'arrêtent la veille de sa date de début ; les autres sont désactivées.
     */
    public GrilleDetail publier(UUID id, String acteur) {
        GrilleTarifaire grille = chargerModifiable(id);
        List<ValidationIssue> erreurs = validator.valider(grille);
        if (!erreurs.isEmpty()) {
            throw TarifException.configurationInvalide("La grille " + grille.libelleVersion()
                    + " comporte des anomalies : corrigez-les avant de la publier.", erreurs);
        }
        reverifierCibles(grille);
        grilles.findByProduitAndDeviseAndStatut(grille.getProduit(), grille.getDevise(), StatutGrille.PUBLIEE).stream()
                .filter(g -> !g.getCode().equals(grille.getCode()))
                .filter(g -> Objects.equals(g.getCanal(), grille.getCanal()))
                .filter(g -> CibleReseau.memeCible(g.getSource(), grille.getSource()))
                .filter(g -> CibleReseau.memeCible(g.getDestination(), grille.getDestination()))
                .filter(g -> chevauchent(g, grille))
                .findFirst()
                .ifPresent(g -> {
                    throw TarifException.conflit("GRILLE_PERIMETRE_OCCUPE", "La grille " + g.libelleVersion()
                            + " couvre déjà ce produit, ce canal, cette devise, cette source et cette destination sur la même période.");
                });
        for (GrilleTarifaire precedente : grilles.findByCodeAndStatut(grille.getCode(), StatutGrille.PUBLIEE)) {
            if (precedente.getDateDebut().isBefore(grille.getDateDebut())) {
                LocalDate veille = grille.getDateDebut().minusDays(1);
                if (precedente.getDateFin() == null || precedente.getDateFin().isAfter(veille)) {
                    precedente.setDateFin(veille);
                }
            } else {
                precedente.setStatut(StatutGrille.DESACTIVEE);
            }
            precedente.setUpdatedBy(acteur);
        }
        grille.setStatut(StatutGrille.PUBLIEE);
        grille.setUpdatedBy(acteur);
        return enregistrer(grille);
    }

    @Transactional(readOnly = true)
    public RapportValidation valider(UUID id) {
        List<ValidationIssue> erreurs = validator.valider(charger(id));
        return new RapportValidation(erreurs.isEmpty(), erreurs);
    }

    /** Calcule les frais d'un montant avec cette grille, quel que soit son statut ; rien n'est enregistré. */
    @Transactional(readOnly = true, noRollbackFor = TarifException.class)
    public ResultatFrais simuler(UUID id, SimulationRequest req) {
        LocalDate date = req.dateCalcul() != null ? req.dateCalcul() : LocalDate.now(clock);
        return moteur.appliquer(charger(id), req.montant(), date);
    }

    /** Flush pour obtenir les identifiants des nouvelles tranches et la date de modification. */
    private GrilleDetail enregistrer(GrilleTarifaire grille) {
        grilles.flush();
        return GrilleDetail.from(grille);
    }

    private GrilleTarifaire charger(UUID id) {
        return grilles.findById(id).orElseThrow(() ->
                TarifException.introuvable("GRILLE_INEXISTANTE", "Grille " + id + " introuvable."));
    }

    private GrilleTarifaire chargerModifiable(UUID id) {
        GrilleTarifaire grille = charger(id);
        if (!grille.estModifiable()) {
            throw TarifException.conflit("GRILLE_NON_MODIFIABLE",
                    "La grille " + grille.libelleVersion() + " est au statut " + grille.getStatut()
                            + " : seule une grille en brouillon est modifiable ; créez une nouvelle version.");
        }
        return grille;
    }

    private static TrancheTarifaire tranche(GrilleTarifaire grille, UUID trancheId) {
        return grille.getTranches().stream()
                .filter(t -> t.getId().equals(trancheId))
                .findFirst()
                .orElseThrow(() -> TarifException.introuvable("TRANCHE_INEXISTANTE",
                        "Tranche " + trancheId + " introuvable dans " + grille.libelleVersion() + "."));
    }

    private static TrancheTarifaire copier(TrancheTarifaire source) {
        return TrancheTarifaire.builder()
                .ordre(source.getOrdre())
                .montantMin(source.getMontantMin())
                .montantMax(source.getMontantMax())
                .formule(source.getFormule())
                .parametres(new LinkedHashMap<>(source.getParametres()))
                .fraisMin(source.getFraisMin())
                .fraisMax(source.getFraisMax())
                .build();
    }

    private static boolean chevauchent(GrilleTarifaire a, GrilleTarifaire b) {
        return (b.getDateFin() == null || !a.getDateDebut().isAfter(b.getDateFin()))
                && (a.getDateFin() == null || !b.getDateDebut().isAfter(a.getDateFin()));
    }

    private void appliquerEntete(GrilleTarifaire grille, GrilleRequest req) {
        if (req.dateFin() != null && req.dateFin().isBefore(req.dateDebut())) {
            throw TarifException.configurationInvalide("Période de validité invalide.", List.of(
                    ValidationIssue.grille("GRILLE_DATES_INVALIDES", "dateFin", "La date de fin précède la date de début.")));
        }
        grille.setLibelle(req.libelle().trim());
        grille.setProduit(code(req.produit()));
        grille.setCanal(code(req.canal()));
        grille.setDevise(code(req.devise()));
        Resolue source = cible(req.source(), "source");
        Resolue destination = cible(req.destination(), "destination");
        if (source != null && !source.devise().equals(grille.getDevise())) {
            throw TarifException.configurationInvalide("Devise incohérente avec la source.", List.of(
                    ValidationIssue.grille("CIBLE_DEVISE_INCOHERENTE", "devise", "La source est en zone "
                            + source.cible().getZone() + " (" + source.devise() + ") : la grille doit être en "
                            + source.devise() + ".")));
        }
        grille.setSource(source == null ? null : source.cible());
        grille.setDestination(destination == null ? null : destination.cible());
        grille.setDateDebut(req.dateDebut());
        grille.setDateFin(req.dateFin());
        grille.setContinuiteRequise(req.continuiteRequise() == null || req.continuiteRequise());
        grille.setArrondiEchelle(req.arrondiEchelle() != null ? req.arrondiEchelle() : 0);
        grille.setArrondiMode(req.arrondiMode() != null ? req.arrondiMode() : RoundingMode.HALF_UP);
        var base = req.baseCalcul() != null
                ? req.baseCalcul()
                : com.gateway.payment.pricing.tarif.domain.BaseCalculFrais.TRANSACTION_AMOUNT;
        var period = AssietteFrais.estCumul(base) ? req.cumulPeriod() : null;
        var scope = AssietteFrais.estCumul(base) ? req.cumulScope() : null;
        AssietteFrais.validerConfig(base, period, scope);
        grille.setBaseCalcul(base);
        grille.setCumulPeriod(period);
        grille.setCumulScope(scope);
    }

    private record Resolue(CibleReseau cible, String devise) {
    }

    /** Vide : toutes. Unité : existante, active, zone recopiée. Zone seule : existante. */
    private Resolue cible(CibleRequest req, String cote) {
        if (req == null || (req.uniteId() == null && !notBlank(req.zone()))) {
            return null;
        }
        if (req.uniteId() == null) {
            PositionReseau p = lire(() -> reseau.zone(code(req.zone())), cote + ".zone");
            return new Resolue(CibleReseau.zone(p.zone()), p.devise());
        }
        PositionReseau p = lire(() -> reseau.unite(req.uniteId()), cote + ".uniteId");
        PositionReseau.Unite u = p.unite();
        if (!u.estActive()) {
            throw cibleInvalide(cote + ".uniteId", "CIBLE_INACTIVE",
                    u.code() + " est inactive : une grille ne peut viser qu'une unité active.");
        }
        if (notBlank(req.zone()) && !code(req.zone()).equals(p.zone())) {
            throw cibleInvalide(cote + ".zone", "CIBLE_ZONE_INCOHERENTE",
                    u.code() + " est en zone " + p.zone() + ", pas en zone " + code(req.zone()) + ".");
        }
        UUID parent = p.lignee().size() > 1 ? p.lignee().get(1).id() : null;
        return new Resolue(new CibleReseau(p.zone(), u.id(), u.code(), u.libelle(), u.type(), parent), p.devise());
    }

    /** À la publication : les unités visées existent toujours et sont actives ; leur code et libellé sont rafraîchis. */
    private void reverifierCibles(GrilleTarifaire grille) {
        grille.setSource(reverifier(grille.getSource(), "source"));
        grille.setDestination(reverifier(grille.getDestination(), "destination"));
    }

    private CibleReseau reverifier(CibleReseau c, String cote) {
        if (CibleReseau.niveau(c) != CibleReseau.Niveau.UNITE) {
            return c;
        }
        Resolue r = cible(new CibleRequest(null, c.getUniteId()), cote);
        return r.cible();
    }

    private PositionReseau lire(Supplier<PositionReseau> lecture, String champ) {
        try {
            return lecture.get();
        } catch (ReseauException e) {
            if (e.isIntrouvable()) {
                throw cibleInvalide(champ, "CIBLE_INCONNUE", e.getMessage());
            }
            throw new TarifException(HttpStatus.SERVICE_UNAVAILABLE, "RESEAU_INDISPONIBLE", e.getMessage());
        }
    }

    private static TarifException cibleInvalide(String champ, String code, String message) {
        return TarifException.configurationInvalide("Cible réseau invalide.", List.of(
                ValidationIssue.grille(code, champ, message)));
    }

    private TrancheTarifaire construire(TrancheRequest req) {
        FormuleRef ref = req.formule();
        Formule formule = formules.findByCodeAndVersion(code(ref.code()), ref.version())
                .orElseThrow(() -> TarifException.configurationInvalide("Formule inconnue.", List.of(
                        ValidationIssue.grille("FORMULE_INCONNUE", "formule",
                                "La formule " + ref.code() + " v" + ref.version() + " n'existe pas au catalogue."))));
        return TrancheTarifaire.builder()
                .montantMin(req.montantMin())
                .montantMax(req.montantMax())
                .formule(formule)
                .parametres(req.parametres() != null ? new LinkedHashMap<>(req.parametres()) : new LinkedHashMap<>())
                .fraisMin(req.fraisMin())
                .fraisMax(req.fraisMax())
                .build();
    }

    /** Renumérote par montant minimum puis refuse l'enregistrement si une des tranches touchées est invalide. */
    private void verifierTranches(GrilleTarifaire grille, List<TrancheTarifaire> touchees) {
        renumeroter(grille);
        List<ValidationIssue> erreurs = new ArrayList<>();
        touchees.forEach(t -> erreurs.addAll(validator.validerTranche(t)));
        if (!erreurs.isEmpty()) {
            throw TarifException.configurationInvalide("Configuration de tranche invalide.", erreurs);
        }
    }

    private static void renumeroter(GrilleTarifaire grille) {
        grille.getTranches().sort(Comparator.comparing(TrancheTarifaire::getMontantMin,
                Comparator.nullsLast(Comparator.naturalOrder())));
        for (int i = 0; i < grille.getTranches().size(); i++) {
            grille.getTranches().get(i).setOrdre(i + 1);
        }
    }

    private static Specification<GrilleTarifaire> specification(Filtre f) {
        List<Specification<GrilleTarifaire>> specs = new ArrayList<>();
        if (notBlank(f.code())) {
            specs.add((root, q, cb) -> cb.equal(root.get("code"), code(f.code())));
        }
        if (notBlank(f.libelle())) {
            String motif = "%" + f.libelle().trim().toLowerCase(Locale.ROOT) + "%";
            specs.add((root, q, cb) -> cb.like(cb.lower(root.get("libelle")), motif));
        }
        if (notBlank(f.produit())) {
            specs.add((root, q, cb) -> cb.equal(root.get("produit"), code(f.produit())));
        }
        if (notBlank(f.canal())) {
            specs.add((root, q, cb) -> cb.equal(root.get("canal"), code(f.canal())));
        }
        if (notBlank(f.devise())) {
            specs.add((root, q, cb) -> cb.equal(root.get("devise"), code(f.devise())));
        }
        if (notBlank(f.source())) {
            specs.add(cibleEgale("source", code(f.source())));
        }
        if (notBlank(f.destination())) {
            specs.add(cibleEgale("destination", code(f.destination())));
        }
        if (f.statut() != null) {
            specs.add((root, q, cb) -> cb.equal(root.get("statut"), f.statut()));
        }
        if (f.date() != null) {
            specs.add((root, q, cb) -> cb.and(
                    cb.lessThanOrEqualTo(root.get("dateDebut"), f.date()),
                    cb.or(cb.isNull(root.get("dateFin")), cb.greaterThanOrEqualTo(root.get("dateFin"), f.date()))));
        }
        return Specification.allOf(specs);
    }

    /** Grilles qui visent exactement cette zone (sans unité) ou cette unité (par son code). */
    private static Specification<GrilleTarifaire> cibleEgale(String cote, String valeur) {
        return (root, q, cb) -> cb.or(
                cb.and(cb.equal(root.get(cote).get("zone"), valeur), cb.isNull(root.get(cote).get("uniteId"))),
                cb.equal(root.get(cote).get("uniteCode"), valeur));
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String code(String valeur) {
        return notBlank(valeur) ? valeur.trim().toUpperCase(Locale.ROOT) : null;
    }
}
