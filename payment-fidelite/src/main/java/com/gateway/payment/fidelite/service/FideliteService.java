package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.Adhesion;
import com.gateway.payment.fidelite.domain.Echange;
import com.gateway.payment.fidelite.domain.MouvementPoints;
import com.gateway.payment.fidelite.domain.Palier;
import com.gateway.payment.fidelite.domain.CatalogueRecompense;
import com.gateway.payment.fidelite.domain.ProgrammeCatalogueRecompense;
import com.gateway.payment.fidelite.domain.PalierSaisie;
import com.gateway.payment.fidelite.domain.PaliersStandard;
import com.gateway.payment.fidelite.domain.Parrainage;
import com.gateway.payment.fidelite.domain.ParrainageStatut;
import com.gateway.payment.fidelite.domain.Programme;
import com.gateway.payment.fidelite.domain.ProgrammePortee;
import com.gateway.payment.fidelite.domain.ProgrammeStatut;
import com.gateway.payment.fidelite.repository.AdhesionRepository;
import com.gateway.payment.fidelite.repository.EchangeRepository;
import com.gateway.payment.fidelite.repository.PalierRepository;
import com.gateway.payment.fidelite.repository.CatalogueRecompenseRepository;
import com.gateway.payment.fidelite.repository.ProgrammeCatalogueRecompenseRepository;
import com.gateway.payment.fidelite.repository.ParrainageRepository;
import com.gateway.payment.fidelite.repository.ProgrammeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FideliteService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ProgrammeRepository programmes;
    private final AdhesionRepository adhesions;
    private final ParrainageRepository parrainages;
    private final PalierRepository paliers;
    private final ProgrammeCatalogueRecompenseRepository offres;
    private final CatalogueRecompenseRepository catalogues;
    private final EchangeRepository echanges;
    private final LedgerService ledger;
    private final ReferralQualificationService referrals;
    private final CommercialActivation commercial;

    public FideliteService(
            ProgrammeRepository programmes,
            AdhesionRepository adhesions,
            ParrainageRepository parrainages,
            PalierRepository paliers,
            ProgrammeCatalogueRecompenseRepository offres,
            CatalogueRecompenseRepository catalogues,
            EchangeRepository echanges,
            LedgerService ledger,
            ReferralQualificationService referrals
    ) {
        this(programmes, adhesions, parrainages, paliers, offres, catalogues, echanges, ledger, referrals, null);
    }

    @Autowired
    public FideliteService(
            ProgrammeRepository programmes,
            AdhesionRepository adhesions,
            ParrainageRepository parrainages,
            PalierRepository paliers,
            ProgrammeCatalogueRecompenseRepository offres,
            CatalogueRecompenseRepository catalogues,
            EchangeRepository echanges,
            LedgerService ledger,
            ReferralQualificationService referrals,
            CommercialActivation commercial
    ) {
        this.programmes = programmes;
        this.adhesions = adhesions;
        this.parrainages = parrainages;
        this.paliers = paliers;
        this.offres = offres;
        this.catalogues = catalogues;
        this.echanges = echanges;
        this.ledger = ledger;
        this.referrals = referrals;
        this.commercial = commercial;
    }

    @Transactional
    public ProgrammeView creer(String tenantId, CreerProgramme commande) {
        String tenant = tenant(tenantId);
        String nom = required(commande.nom(), "Le nom du programme est requis");
        ProgrammePortee portee = commande.portee() == null ? ProgrammePortee.TOUS : commande.portee();
        Set<String> profils = normalize(commande.profils());
        if (portee == ProgrammePortee.PROFIL && profils.isEmpty()) {
            throw new IllegalArgumentException("Au moins un profil client est requis");
        }
        if (portee == ProgrammePortee.UNITE && commande.uniteId() == null) {
            throw new IllegalArgumentException("L'unité qui porte le programme est requise");
        }
        if (portee != ProgrammePortee.PROFIL) {
            profils = Set.of();
        }
        if (portee != ProgrammePortee.UNITE && commande.uniteId() != null) {
            throw new IllegalArgumentException("L'unité ne se renseigne que pour un programme d'unité");
        }
        if (commande.fseId() == null) {
            throw new IllegalArgumentException("Le FSE du programme est requis");
        }
        Programme programme = new Programme();
        programme.setTenantId(tenant);
        programme.setCode(uniqueCode(tenant, nom));
        programme.setUnitCode("POINT");
        appliquer(programme, commande, nom, portee, profils);
        programme.setStatut(ProgrammeStatut.ACTIF);
        programme.setCreatedAt(Instant.now());
        Programme saved = programmes.save(programme);
        List<Palier> niveaux = this.paliers.saveAll(niveaux(saved, commande.paliers()));
        return ProgrammeView.from(saved, niveaux);
    }

    /** Met à jour le libellé, la portée, le FSE, les points et la durée. Le code reste inchangé. */
    @Transactional
    public ProgrammeView modifier(UUID programmeId, CreerProgramme commande, Integer validiteMois) {
        if (validiteMois != null && validiteMois != 3 && validiteMois != 6 && validiteMois != 12) {
            throw new IllegalArgumentException("La durée est de 3, 6 ou 12 mois, ou illimitée");
        }
        String nom = required(commande.nom(), "Le nom du programme est requis");
        ProgrammePortee portee = commande.portee() == null ? ProgrammePortee.TOUS : commande.portee();
        Set<String> profils = normalize(commande.profils());
        if (portee == ProgrammePortee.PROFIL && profils.isEmpty()) {
            throw new IllegalArgumentException("Au moins un profil client est requis");
        }
        if (portee == ProgrammePortee.UNITE && commande.uniteId() == null) {
            throw new IllegalArgumentException("L'unité qui porte le programme est requise");
        }
        if (portee != ProgrammePortee.PROFIL) {
            profils = Set.of();
        }
        if (portee != ProgrammePortee.UNITE && commande.uniteId() != null) {
            throw new IllegalArgumentException("L'unité ne se renseigne que pour un programme d'unité");
        }
        if (commande.fseId() == null) {
            throw new IllegalArgumentException("Le FSE du programme est requis");
        }
        Programme programme = programmes.findById(programmeId)
                .orElseThrow(() -> new NoSuchElementException("Programme introuvable"));
        appliquer(programme, commande, nom, portee, profils);
        programme.setValiditeMois(validiteMois);
        Programme saved = programmes.save(programme);
        return ProgrammeView.from(saved, paliers.findByProgramme_IdOrderBySeuilPointsAsc(programmeId));
    }

    private void appliquer(
            Programme programme,
            CreerProgramme commande,
            String nom,
            ProgrammePortee portee,
            Set<String> profils
    ) {
        programme.setNom(nom);
        programme.setPortee(portee);
        programme.setUniteId(portee == ProgrammePortee.UNITE ? commande.uniteId() : null);
        programme.setUniteNom(portee == ProgrammePortee.UNITE ? blank(commande.uniteNom()) : null);
        programme.setFseId(commande.fseId());
        programme.setFseNom(required(commande.fseNom(), "Le nom du FSE est requis"));
        programme.setPointsParMille(Math.max(0, commande.pointsParMille()));
        programme.setPointsParrain(Math.max(0, commande.pointsParrain()));
        programme.setPointsFilleul(Math.max(0, commande.pointsFilleul()));
        programme.setProfils(profils);
    }

    /**
     * Programmes qu'un client peut voir : ceux ouverts à tous, ceux de ses profils,
     * et ceux d'une unité auxquels il a déjà adhéré.
     */
    @Transactional(readOnly = true)
    public List<ProgrammeView> visibles(String tenantId, UUID partyId, Set<String> profilsClient) {
        Set<String> profils = normalize(profilsClient);
        List<Programme> found = programmes.findByTenantIdAndStatutOrderByCreatedAtDesc(tenant(tenantId), ProgrammeStatut.ACTIF)
                .stream()
                .filter(programme -> visible(programme, partyId, profils))
                .toList();
        return views(found);
    }

    @Transactional(readOnly = true)
    public List<ProgrammeView> deLUnite(UUID uniteId) {
        if (uniteId == null) {
            throw new IllegalArgumentException("L'unité est requise");
        }
        return views(programmes.findByUniteIdAndStatutOrderByCreatedAtDesc(uniteId, ProgrammeStatut.ACTIF));
    }

    @Transactional(readOnly = true)
    public List<ClientView> clients(UUID programmeId) {
        actif(programmeId);
        return adhesions.findByProgramme_IdOrderByCreatedAtDesc(programmeId).stream()
                .map(adhesion -> new ClientView(adhesion.getPartyId(), adhesion.getCreatedAt()))
                .toList();
    }

    /** Le marchand inscrit un de ses clients. Le client rejoint lui-même un programme TOUS ou PROFIL. */
    @Transactional
    public void adherer(UUID programmeId, UUID partyId, Set<String> profilsClient, boolean parLUnite) {
        adherer(programmeId, partyId, profilsClient, parLUnite ? Origine.UNITE : Origine.CLIENT);
    }

    private void adherer(UUID programmeId, UUID partyId, Set<String> profilsClient, Origine origine) {
        Programme programme = actif(programmeId);
        if (partyId == null) {
            throw new IllegalArgumentException("Le client est requis");
        }
        if (adhesions.existsByProgramme_IdAndPartyId(programmeId, partyId)) {
            return;
        }
        if (origine == Origine.UNITE && programme.getPortee() != ProgrammePortee.UNITE) {
            throw new IllegalArgumentException("Seul un programme d'unité inscrit ses clients");
        }
        if (programme.getPortee() == ProgrammePortee.UNITE && origine == Origine.CLIENT) {
            throw new IllegalArgumentException("Ce programme est réservé aux clients de l'unité");
        }
        if (programme.getPortee() == ProgrammePortee.PROFIL
                && disjoint(programme.getProfils(), normalize(profilsClient))) {
            throw new IllegalArgumentException("Le profil du client n'ouvre pas ce programme");
        }
        Adhesion adhesion = new Adhesion();
        adhesion.setProgramme(programme);
        adhesion.setPartyId(partyId);
        adhesion.setCreatedAt(Instant.now());
        adhesions.save(adhesion);
    }

    @Transactional
    public ParrainageView parrainer(UUID programmeId, UUID parrainId) {
        Programme programme = actif(programmeId);
        if (parrainId == null) {
            throw new IllegalArgumentException("Le parrain est requis");
        }
        if (!adhesions.existsByProgramme_IdAndPartyId(programmeId, parrainId)) {
            throw new IllegalArgumentException("Le parrain doit déjà être client du programme");
        }
        Parrainage parrainage = new Parrainage();
        parrainage.setProgramme(programme);
        parrainage.setParrainPartyId(parrainId);
        parrainage.setCode(nextCode());
        parrainage.setStatut(ParrainageStatut.OUVERT);
        parrainage.setCreatedAt(Instant.now());
        return ParrainageView.from(parrainages.save(parrainage));
    }

    /**
     * Le filleul rejoint le programme du parrain. Les points attendent le KYC
     * et une première transaction éligible.
     */
    @Transactional
    public ParrainageView accepter(String code, UUID filleulId, Set<String> profilsClient) {
        if (filleulId == null) {
            throw new IllegalArgumentException("Le filleul est requis");
        }
        String normalized = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        Parrainage parrainage = parrainages.findByCodeIgnoreCase(normalized)
                .orElseThrow(() -> new NoSuchElementException("Code de parrainage introuvable"));
        if (parrainage.getStatut() != ParrainageStatut.OUVERT) {
            throw new IllegalArgumentException("Ce code de parrainage est déjà utilisé");
        }
        if (filleulId.equals(parrainage.getParrainPartyId())) {
            throw new IllegalArgumentException("Le parrain ne peut pas utiliser son propre code");
        }
        Programme programme = parrainage.getProgramme();
        if (programme.getStatut() != ProgrammeStatut.ACTIF) {
            throw new IllegalArgumentException("Le programme est fermé");
        }
        if (programme.getPortee() == ProgrammePortee.PROFIL
                && disjoint(programme.getProfils(), normalize(profilsClient))) {
            throw new IllegalArgumentException("Le profil du filleul n'ouvre pas ce programme");
        }
        if (adhesions.existsByProgramme_IdAndPartyId(programme.getId(), filleulId)) {
            throw new IllegalArgumentException("Ce client est déjà membre du programme");
        }
        if (parrainages.existsByProgramme_IdAndFilleulPartyId(programme.getId(), filleulId)) {
            throw new IllegalArgumentException("Ce client a déjà un parrain pour ce programme");
        }
        adherer(programme.getId(), filleulId, profilsClient, Origine.PARRAINAGE);
        parrainage.setFilleulPartyId(filleulId);
        parrainage.setStatut(ParrainageStatut.ASSOCIE);
        parrainage.setUsedAt(Instant.now());
        parrainage.setKycApproved(referrals.kycDejaRecu(programme.getTenantId(), filleulId));
        return ParrainageView.from(parrainages.save(parrainage));
    }

    @Transactional(readOnly = true)
    public Situation solde(UUID programmeId, UUID partyId) {
        Programme programme = actif(programmeId);
        if (partyId == null) {
            throw new IllegalArgumentException("Le client est requis");
        }
        int points = Math.toIntExact(ledger.disponible(programme.getTenantId(), programmeId, partyId));
        List<Palier> niveaux = paliers.findByProgramme_IdOrderBySeuilPointsAsc(programmeId);
        return situation(programmeId, partyId, points, niveaux);
    }

    @Transactional(readOnly = true)
    public List<MouvementView> mouvements(UUID programmeId, UUID partyId) {
        Programme programme = actif(programmeId);
        if (partyId == null) {
            throw new IllegalArgumentException("Le client est requis");
        }
        return ledger.historique(programme.getTenantId(), programmeId, partyId).stream()
                .map(row -> new MouvementView(
                        row.getId(),
                        Math.toIntExact(row.getPoints()),
                        row.getMotif(),
                        row.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecompenseView> recompenses(UUID programmeId) {
        actif(programmeId);
        return offres.findByProgramme_IdOrderByPositionAsc(programmeId).stream()
                .map(RecompenseView::from)
                .toList();
    }

    /** Retire le coût du solde et conserve l'échange. */
    @Transactional
    public EchangeView echanger(UUID programmeId, String code, UUID partyId) {
        Programme programme = actif(programmeId);
        if (partyId == null) {
            throw new IllegalArgumentException("Le client est requis");
        }
        if (!adhesions.existsByProgramme_IdAndPartyId(programmeId, partyId)) {
            throw new IllegalArgumentException("Le client doit d'abord adhérer au programme");
        }
        String normalized = code == null ? "" : code.trim();
        ProgrammeCatalogueRecompense offre = offres.findByProgramme_IdAndCatalogue_CodeIgnoreCase(programmeId, normalized)
                .orElseThrow(() -> new NoSuchElementException("Récompense introuvable"));
        CatalogueRecompense catalogue = offre.getCatalogue();
        Instant maintenant = Instant.now();
        if (offre.getValideDu() != null && maintenant.isBefore(offre.getValideDu())) {
            throw new IllegalArgumentException("Cette récompense n'est pas encore disponible");
        }
        if (offre.getValideAu() != null && !maintenant.isBefore(offre.getValideAu())) {
            throw new IllegalArgumentException("Cette récompense n'est plus disponible");
        }
        if (offre.getStockRestant() != null && offre.getStockRestant() <= 0) {
            throw new IllegalArgumentException("Cette récompense n'est plus disponible");
        }
        String reference = "ECHANGE:" + UUID.randomUUID();
        long restants = ledger.consommer(
                programme.getTenantId(),
                programmeId,
                partyId,
                catalogue.getCout(),
                catalogue.getTitre(),
                reference);
        if (offre.getStockRestant() != null) {
            offre.setStockRestant(offre.getStockRestant() - 1);
            offres.save(offre);
        }
        Echange echange = new Echange();
        echange.setRecompense(offre);
        echange.setProgramme(programme);
        echange.setPartyId(partyId);
        echange.setPoints(catalogue.getCout());
        echange.setStatut("FULFILLED");
        echange.setBusinessReference(reference);
        echange.setCreatedAt(Instant.now());
        if (offre.getTauxCashback() != null) {
            echange.setValeurXof(offre.getTauxCashback().multiply(BigDecimal.valueOf(catalogue.getCout())));
        }
        echanges.save(echange);
        if (commercial != null) {
            commercial.usage(programme, offre.getMarchandId(), partyId, catalogue.getCout(), reference);
        }
        return new EchangeView(catalogue.getCode(), catalogue.getTitre(), Math.toIntExact(restants));
    }

    @Transactional
    public void definirValidite(UUID programmeId, Integer mois) {
        if (mois != null && mois != 3 && mois != 6 && mois != 12) {
            throw new IllegalArgumentException("La durée est de 3, 6 ou 12 mois, ou illimitée");
        }
        Programme programme = programmes.findById(programmeId)
                .orElseThrow(() -> new NoSuchElementException("Programme introuvable"));
        programme.setValiditeMois(mois);
        programmes.save(programme);
    }

    @Transactional
    public RecompenseView ajouterRecompense(UUID programmeId, RecompenseDynamique saisie) {
        Programme programme = actif(programmeId);
        String code = required(saisie.code(), "Le code de la récompense est requis").toUpperCase(Locale.ROOT);
        if (offres.findByProgramme_IdAndCatalogue_CodeIgnoreCase(programmeId, code).isPresent()) {
            throw new IllegalArgumentException("Cette récompense existe déjà");
        }
        if (saisie.cout() <= 0) {
            throw new IllegalArgumentException("Le coût est positif");
        }
        CatalogueRecompense catalogue = catalogues.findByTenantIdAndCodeIgnoreCase(programme.getTenantId(), code)
                .orElseGet(() -> catalogues.save(nouveauCatalogue(programme, code, saisie)));
        ProgrammeCatalogueRecompense offre = new ProgrammeCatalogueRecompense();
        offre.setProgramme(programme);
        offre.setCatalogue(catalogue);
        offre.setPosition(offres.findByProgramme_IdOrderByPositionAsc(programmeId).size());
        renseignerOffre(offre, saisie);
        return RecompenseView.from(offres.save(offre));
    }

    @Transactional
    public RecompenseView modifierRecompense(UUID programmeId, String codeActuel, RecompenseDynamique saisie) {
        Programme programme = actif(programmeId);
        ProgrammeCatalogueRecompense offre = offres.findByProgramme_IdAndCatalogue_CodeIgnoreCase(programmeId, codeActuel)
                .orElseThrow(() -> new IllegalArgumentException("Cette récompense n'appartient pas à ce programme"));
        String code = required(saisie.code(), "Le code de la récompense est requis").toUpperCase(Locale.ROOT);
        if (!code.equalsIgnoreCase(offre.getCatalogue().getCode())
                && offres.findByProgramme_IdAndCatalogue_CodeIgnoreCase(programmeId, code).isPresent()) {
            throw new IllegalArgumentException("Cette récompense existe déjà");
        }
        if (saisie.cout() <= 0) {
            throw new IllegalArgumentException("Le coût est positif");
        }
        CatalogueRecompense catalogue = catalogues.findByTenantIdAndCodeIgnoreCase(programme.getTenantId(), code)
                .orElseGet(() -> {
                    CatalogueRecompense created = nouveauCatalogue(programme, code, saisie);
                    created.setIcone(offre.getCatalogue().getIcone());
                    return catalogues.save(created);
                });
        renseignerCatalogue(catalogue, saisie);
        offre.setCatalogue(catalogues.save(catalogue));
        renseignerOffre(offre, saisie);
        return RecompenseView.from(offres.save(offre));
    }

    @Transactional(readOnly = true)
    public List<CatalogueView> catalogue(String tenantId) {
        return catalogues.findByTenantIdOrderByCodeAsc(tenant(tenantId)).stream()
                .map(CatalogueView::from)
                .toList();
    }

    @Transactional
    public CatalogueView creerCatalogue(String tenantId, CatalogueSaisie saisie) {
        String tenant = tenant(tenantId);
        String code = codeCatalogue(saisie.code());
        if (catalogues.findByTenantIdAndCodeIgnoreCase(tenant, code).isPresent()) {
            throw new IllegalArgumentException("Cette récompense existe déjà");
        }
        if (saisie.cout() <= 0) {
            throw new IllegalArgumentException("Le coût est positif");
        }
        CatalogueRecompense row = new CatalogueRecompense();
        row.setTenantId(tenant);
        row.setCode(code);
        row.setIcone("gift-outline");
        ecrireCatalogue(row, saisie);
        return CatalogueView.from(catalogues.save(row));
    }

    @Transactional
    public CatalogueView modifierCatalogue(String tenantId, String codeActuel, CatalogueSaisie saisie) {
        String tenant = tenant(tenantId);
        CatalogueRecompense row = catalogues.findByTenantIdAndCodeIgnoreCase(tenant, codeActuel)
                .orElseThrow(() -> new IllegalArgumentException("Récompense introuvable"));
        String code = codeCatalogue(saisie.code());
        if (!code.equalsIgnoreCase(row.getCode())
                && catalogues.findByTenantIdAndCodeIgnoreCase(tenant, code).isPresent()) {
            throw new IllegalArgumentException("Cette récompense existe déjà");
        }
        if (saisie.cout() <= 0) {
            throw new IllegalArgumentException("Le coût est positif");
        }
        row.setCode(code);
        ecrireCatalogue(row, saisie);
        return CatalogueView.from(catalogues.save(row));
    }

    private static String codeCatalogue(String code) {
        return required(code, "Le code de la récompense est requis").toUpperCase(Locale.ROOT);
    }

    private static void ecrireCatalogue(CatalogueRecompense row, CatalogueSaisie saisie) {
        String type = saisie.type() == null || saisie.type().isBlank() ? "CATALOGUE" : saisie.type().trim().toUpperCase(Locale.ROOT);
        row.setTitre(required(saisie.titre(), "Le titre est requis"));
        row.setDescription(required(saisie.description(), "La description est requise"));
        row.setCout(saisie.cout());
        row.setCategorie(type);
        row.setTypeRecompense(type);
    }

    private CatalogueRecompense nouveauCatalogue(Programme programme, String code, RecompenseDynamique saisie) {
        CatalogueRecompense catalogue = new CatalogueRecompense();
        catalogue.setTenantId(programme.getTenantId());
        catalogue.setCode(code);
        catalogue.setIcone("gift-outline");
        renseignerCatalogue(catalogue, saisie);
        return catalogue;
    }

    private void renseignerCatalogue(CatalogueRecompense catalogue, RecompenseDynamique saisie) {
        String type = saisie.type() == null ? "CATALOGUE" : saisie.type().trim().toUpperCase(Locale.ROOT);
        catalogue.setTitre(required(saisie.titre(), "Le titre est requis"));
        catalogue.setDescription(required(saisie.description(), "La description est requise"));
        catalogue.setCout(saisie.cout());
        catalogue.setCategorie(type);
        catalogue.setTypeRecompense(type);
    }

    private static void renseignerOffre(ProgrammeCatalogueRecompense offre, RecompenseDynamique saisie) {
        offre.setStock(saisie.stock());
        offre.setStockRestant(saisie.stock());
        offre.setValideDu(saisie.valideDu());
        offre.setValideAu(saisie.valideAu());
        offre.setMarchandId(saisie.marchandId());
        offre.setTauxCashback(saisie.tauxCashback());
        offre.setFinanceurId(saisie.financeurId());
    }

    private List<ProgrammeView> views(List<Programme> found) {
        if (found.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<Palier>> byProgramme = paliers
                .findByProgramme_IdInOrderBySeuilPointsAsc(found.stream().map(Programme::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(palier -> palier.getProgramme().getId()));
        return found.stream()
                .map(programme -> ProgrammeView.from(programme, byProgramme.getOrDefault(programme.getId(), List.of())))
                .toList();
    }

    private List<Palier> niveaux(Programme programme, List<PalierSaisie> demandes) {
        List<PalierSaisie> source = demandes == null || demandes.isEmpty() ? PaliersStandard.DEFAUT : demandes;
        Set<String> codes = new HashSet<>();
        Set<Integer> seuils = new HashSet<>();
        List<Palier> rows = new ArrayList<>();
        for (PalierSaisie saisie : source) {
            String code = required(saisie.code(), "Le code du palier est requis").toUpperCase(Locale.ROOT);
            String label = required(saisie.label(), "Le nom du palier est requis");
            if (saisie.seuilPoints() < 0) {
                throw new IllegalArgumentException("Le seuil d'un palier est positif");
            }
            if (!codes.add(code) || !seuils.add(saisie.seuilPoints())) {
                throw new IllegalArgumentException("Chaque palier a un code et un seuil uniques");
            }
            BigDecimal rate = saisie.pointsParMille() == null ? BigDecimal.ZERO : saisie.pointsParMille();
            if (rate.signum() < 0) {
                throw new IllegalArgumentException("Le gain d'un palier est positif");
            }
            Palier palier = new Palier();
            palier.setProgramme(programme);
            palier.setCode(code);
            palier.setLabel(label);
            palier.setSeuilPoints(saisie.seuilPoints());
            palier.setPointsParMille(rate);
            palier.setAvantages(avantages(saisie.avantages()));
            rows.add(palier);
        }
        if (!seuils.contains(0)) {
            throw new IllegalArgumentException("Un palier doit partir de 0 point");
        }
        rows.sort(Comparator.comparingInt(Palier::getSeuilPoints));
        return rows;
    }

    static Situation situation(UUID programmeId, UUID partyId, int points, List<Palier> niveaux) {
        List<Palier> ordered = niveaux.stream()
                .sorted(Comparator.comparingInt(Palier::getSeuilPoints))
                .toList();
        Palier courant = null;
        Palier suivant = null;
        for (Palier palier : ordered) {
            if (palier.getSeuilPoints() <= points) {
                courant = palier;
            } else if (suivant == null) {
                suivant = palier;
            }
        }
        int reste = suivant == null ? 0 : Math.max(0, suivant.getSeuilPoints() - points);
        return new Situation(programmeId, partyId, points, PalierView.from(courant), PalierView.from(suivant), reste);
    }

    private static List<String> avantages(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
    }

    private String uniqueCode(String tenant, String nom) {
        String base = nom.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "-").replaceAll("^-+|-+$", "");
        if (base.isBlank()) {
            base = "PRG";
        }
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }
        String code = base;
        int suffix = 2;
        while (programmes.existsByTenantIdAndCode(tenant, code)) {
            code = base + "-" + suffix++;
        }
        return code;
    }

    private boolean visible(Programme programme, UUID partyId, Set<String> profils) {
        return switch (programme.getPortee()) {
            case TOUS -> true;
            case PROFIL -> !disjoint(programme.getProfils(), profils);
            case UNITE -> partyId != null && adhesions.existsByProgramme_IdAndPartyId(programme.getId(), partyId);
        };
    }

    private Programme actif(UUID id) {
        Programme programme = programmes.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Programme introuvable"));
        if (programme.getStatut() != ProgrammeStatut.ACTIF) {
            throw new IllegalArgumentException("Le programme est fermé");
        }
        return programme;
    }

    private Programme duTenant(String tenantId, UUID id) {
        Programme programme = programmes.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Programme introuvable"));
        if (!programme.getTenantId().equals(tenant(tenantId))) {
            throw new NoSuchElementException("Programme introuvable");
        }
        return programme;
    }

    private String nextCode() {
        for (int attempt = 0; attempt < 8; attempt++) {
            StringBuilder code = new StringBuilder("PAR-");
            for (int i = 0; i < 4; i++) {
                code.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            if (!parrainages.existsByCode(code.toString())) {
                return code.toString();
            }
        }
        throw new IllegalStateException("Impossible de générer un code de parrainage");
    }

    private static boolean disjoint(Set<String> programme, Set<String> client) {
        for (String code : programme) {
            if (client.contains(code)) {
                return false;
            }
        }
        return true;
    }

    private static Set<String> normalize(Set<String> codes) {
        if (codes == null) {
            return Set.of();
        }
        return codes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(code -> code.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private static String tenant(String tenantId) {
        return tenantId == null || tenantId.isBlank() ? "CORIS_DEMO" : tenantId.trim();
    }

    private static String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private enum Origine {
        CLIENT,
        UNITE,
        PARRAINAGE
    }

    public record CreerProgramme(
            String nom,
            ProgrammePortee portee,
            UUID uniteId,
            String uniteNom,
            Set<String> profils,
            int pointsParMille,
            int pointsParrain,
            int pointsFilleul,
            List<PalierSaisie> paliers,
            UUID fseId,
            String fseNom
    ) {
    }

    public record CatalogueSaisie(String code, String titre, String description, int cout, String type) {
    }

    public record CatalogueView(
            String code,
            String titre,
            String description,
            int cout,
            String icone,
            String categorie,
            String type
    ) {
        static CatalogueView from(CatalogueRecompense row) {
            return new CatalogueView(
                    row.getCode(),
                    row.getTitre(),
                    row.getDescription(),
                    row.getCout(),
                    row.getIcone(),
                    row.getCategorie(),
                    row.getTypeRecompense());
        }
    }

    public record RecompenseDynamique(
            String code,
            String titre,
            String description,
            int cout,
            String type,
            Integer stock,
            Instant valideDu,
            Instant valideAu,
            UUID marchandId,
            BigDecimal tauxCashback,
            UUID financeurId
    ) {
    }

    public record PalierView(
            String code,
            String label,
            int seuilPoints,
            BigDecimal pointsParMille,
            List<String> avantages
    ) {
        static PalierView from(Palier palier) {
            if (palier == null) {
                return null;
            }
            return new PalierView(
                    palier.getCode(),
                    palier.getLabel(),
                    palier.getSeuilPoints(),
                    palier.getPointsParMille(),
                    List.copyOf(palier.getAvantages()));
        }
    }

    public record RecompenseView(
            String code,
            String titre,
            String description,
            int cout,
            String icone,
            String categorie,
            String type,
            Integer stockRestant,
            Instant valideDu,
            Instant valideAu,
            UUID marchandId,
            BigDecimal tauxCashback,
            UUID financeurId
    ) {
        static RecompenseView from(ProgrammeCatalogueRecompense offre) {
            CatalogueRecompense catalogue = offre.getCatalogue();
            return new RecompenseView(
                    catalogue.getCode(),
                    catalogue.getTitre(),
                    catalogue.getDescription(),
                    catalogue.getCout(),
                    catalogue.getIcone(),
                    catalogue.getCategorie(),
                    catalogue.getTypeRecompense(),
                    offre.getStockRestant(),
                    offre.getValideDu(),
                    offre.getValideAu(),
                    offre.getMarchandId(),
                    offre.getTauxCashback(),
                    offre.getFinanceurId());
        }
    }

    public record EchangeView(String code, String titre, int pointsRestants) {
    }

    public record MouvementView(UUID id, int points, String motif, Instant createdAt) {
        static MouvementView from(MouvementPoints mouvement) {
            return new MouvementView(
                    mouvement.getId(),
                    mouvement.getPoints(),
                    mouvement.getMotif(),
                    mouvement.getCreatedAt());
        }
    }

    public record Situation(
            UUID programmeId,
            UUID partyId,
            int points,
            PalierView palier,
            PalierView palierSuivant,
            int pointsPourSuivant
    ) {
    }

    public record ClientView(UUID partyId, Instant createdAt) {
    }

    public record ProgrammeView(
            UUID id,
            String tenantId,
            String nom,
            ProgrammePortee portee,
            UUID uniteId,
            String uniteNom,
            Set<String> profils,
            int pointsParMille,
            int pointsParrain,
            int pointsFilleul,
            ProgrammeStatut statut,
            List<PalierView> paliers,
            Integer validiteMois,
            UUID fseId,
            String fseNom,
            boolean referralKycRequired,
            BigDecimal referralMinAmount,
            int referralQualificationDays,
            int referralMaxPerMonth,
            String referralProducts
    ) {
        static ProgrammeView from(Programme programme, List<Palier> niveaux) {
            List<PalierView> views = niveaux.stream()
                    .sorted(Comparator.comparingInt(Palier::getSeuilPoints))
                    .map(PalierView::from)
                    .toList();
            return new ProgrammeView(
                    programme.getId(),
                    programme.getTenantId(),
                    programme.getNom(),
                    programme.getPortee(),
                    programme.getUniteId(),
                    programme.getUniteNom(),
                    Set.copyOf(programme.getProfils()),
                    programme.getPointsParMille(),
                    programme.getPointsParrain(),
                    programme.getPointsFilleul(),
                    programme.getStatut(),
                    views,
                    programme.getValiditeMois(),
                    programme.getFseId(),
                    programme.getFseNom(),
                    programme.isReferralKycRequired(),
                    programme.getReferralMinAmount(),
                    programme.getReferralQualificationDays(),
                    programme.getReferralMaxPerMonth(),
                    programme.getReferralProducts());
        }
    }

    @Transactional(readOnly = true)
    public List<ParrainageSuivi> parrainagesDuProgramme(String tenantId, UUID programmeId) {
        duTenant(tenantId, programmeId);
        return parrainages.findByProgramme_IdOrderByCreatedAtDesc(programmeId).stream()
                .map(ParrainageSuivi::from)
                .toList();
    }

    @Transactional
    public ProgrammeView regleParrainage(String tenantId, UUID programmeId, RegleParrainage regle) {
        if (regle.montantMinimum() == null || regle.montantMinimum().signum() < 0) {
            throw new IllegalArgumentException("Le montant minimum est requis");
        }
        if (regle.delaiJours() < 0 || regle.plafondMois() < 0) {
            throw new IllegalArgumentException("Le délai et le plafond sont positifs ou nuls");
        }
        Programme programme = duTenant(tenantId, programmeId);
        programme.setReferralKycRequired(regle.kycRequis());
        programme.setReferralMinAmount(regle.montantMinimum());
        programme.setReferralQualificationDays(regle.delaiJours());
        programme.setReferralMaxPerMonth(regle.plafondMois());
        String produits = regle.produits() == null ? "" : regle.produits().trim();
        programme.setReferralProducts(produits.isEmpty() ? null : produits);
        Programme saved = programmes.save(programme);
        return ProgrammeView.from(saved, paliers.findByProgramme_IdOrderBySeuilPointsAsc(programmeId));
    }

    public record RegleParrainage(
            boolean kycRequis,
            BigDecimal montantMinimum,
            int delaiJours,
            int plafondMois,
            String produits
    ) {
    }

    @Transactional(readOnly = true)
    public List<ParrainageSuivi> parrainages(String tenantId) {
        return parrainages.findByProgramme_TenantIdOrderByCreatedAtDesc(tenant(tenantId)).stream()
                .map(ParrainageSuivi::from)
                .toList();
    }

    public record ParrainageSuivi(
            UUID id,
            UUID programmeId,
            String programmeNom,
            UUID parrainPartyId,
            UUID filleulPartyId,
            String code,
            ParrainageStatut statut,
            Instant createdAt,
            Instant usedAt,
            Instant qualifiedAt,
            boolean kycApproved,
            String rejectionReason,
            UUID eligibleEventId,
            Instant eligibleAt
    ) {
        static ParrainageSuivi from(Parrainage parrainage) {
            return new ParrainageSuivi(
                    parrainage.getId(),
                    parrainage.getProgramme().getId(),
                    parrainage.getProgramme().getNom(),
                    parrainage.getParrainPartyId(),
                    parrainage.getFilleulPartyId(),
                    parrainage.getCode(),
                    parrainage.getStatut(),
                    parrainage.getCreatedAt(),
                    parrainage.getUsedAt(),
                    parrainage.getQualifiedAt(),
                    parrainage.isKycApproved(),
                    parrainage.getRejectionReason(),
                    parrainage.getEligibleEventId(),
                    parrainage.getEligibleAt());
        }
    }

    public record ParrainageView(
            UUID id,
            UUID programmeId,
            UUID parrainPartyId,
            UUID filleulPartyId,
            String code,
            ParrainageStatut statut
    ) {
        static ParrainageView from(Parrainage parrainage) {
            return new ParrainageView(
                    parrainage.getId(),
                    parrainage.getProgramme().getId(),
                    parrainage.getParrainPartyId(),
                    parrainage.getFilleulPartyId(),
                    parrainage.getCode(),
                    parrainage.getStatut());
        }
    }
}
