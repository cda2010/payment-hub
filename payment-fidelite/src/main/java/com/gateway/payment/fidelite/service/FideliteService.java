package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.Adhesion;
import com.gateway.payment.fidelite.domain.Echange;
import com.gateway.payment.fidelite.domain.MouvementPoints;
import com.gateway.payment.fidelite.domain.Palier;
import com.gateway.payment.fidelite.domain.Recompense;
import com.gateway.payment.fidelite.domain.RecompenseSaisie;
import com.gateway.payment.fidelite.domain.RecompensesStandard;
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
import com.gateway.payment.fidelite.repository.RecompenseRepository;
import com.gateway.payment.fidelite.repository.ParrainageRepository;
import com.gateway.payment.fidelite.repository.ProgrammeRepository;
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
    private final RecompenseRepository recompenses;
    private final EchangeRepository echanges;
    private final LedgerService ledger;
    private final ReferralQualificationService referrals;

    public FideliteService(
            ProgrammeRepository programmes,
            AdhesionRepository adhesions,
            ParrainageRepository parrainages,
            PalierRepository paliers,
            RecompenseRepository recompenses,
            EchangeRepository echanges,
            LedgerService ledger,
            ReferralQualificationService referrals
    ) {
        this.programmes = programmes;
        this.adhesions = adhesions;
        this.parrainages = parrainages;
        this.paliers = paliers;
        this.recompenses = recompenses;
        this.echanges = echanges;
        this.ledger = ledger;
        this.referrals = referrals;
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
        Programme programme = new Programme();
        programme.setTenantId(tenant);
        programme.setCode(uniqueCode(tenant, nom));
        programme.setUnitCode("POINT");
        programme.setNom(nom);
        programme.setPortee(portee);
        programme.setUniteId(portee == ProgrammePortee.UNITE ? commande.uniteId() : null);
        programme.setUniteNom(portee == ProgrammePortee.UNITE ? blank(commande.uniteNom()) : null);
        programme.setPointsParMille(Math.max(0, commande.pointsParMille()));
        programme.setPointsParrain(Math.max(0, commande.pointsParrain()));
        programme.setPointsFilleul(Math.max(0, commande.pointsFilleul()));
        programme.setStatut(ProgrammeStatut.ACTIF);
        programme.setCreatedAt(Instant.now());
        programme.setProfils(profils);
        Programme saved = programmes.save(programme);
        List<Palier> niveaux = this.paliers.saveAll(niveaux(saved, commande.paliers()));
        this.recompenses.saveAll(catalogue(saved));
        return ProgrammeView.from(saved, niveaux);
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
        return recompenses.findByProgramme_IdOrderByPositionAsc(programmeId).stream()
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
        Recompense recompense = recompenses.findByProgramme_IdAndCodeIgnoreCase(programmeId, normalized)
                .orElseThrow(() -> new NoSuchElementException("Récompense introuvable"));
        String reference = "ECHANGE:" + UUID.randomUUID();
        long restants = ledger.consommer(
                programme.getTenantId(),
                programmeId,
                partyId,
                recompense.getCout(),
                recompense.getTitre(),
                reference);
        Echange echange = new Echange();
        echange.setRecompense(recompense);
        echange.setProgramme(programme);
        echange.setPartyId(partyId);
        echange.setPoints(recompense.getCout());
        echange.setStatut("FULFILLED");
        echange.setBusinessReference(reference);
        echange.setCreatedAt(Instant.now());
        echanges.save(echange);
        return new EchangeView(recompense.getCode(), recompense.getTitre(), Math.toIntExact(restants));
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

    private List<Recompense> catalogue(Programme programme) {
        List<Recompense> rows = new ArrayList<>();
        int position = 0;
        for (RecompenseSaisie saisie : RecompensesStandard.DEFAUT) {
            Recompense recompense = new Recompense();
            recompense.setProgramme(programme);
            recompense.setCode(saisie.code());
            recompense.setTitre(saisie.titre());
            recompense.setDescription(saisie.description());
            recompense.setCout(saisie.cout());
            recompense.setIcone(saisie.icone());
            recompense.setCategorie(saisie.categorie());
            recompense.setPosition(position++);
            rows.add(recompense);
        }
        return rows;
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
            List<PalierSaisie> paliers
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
            String categorie
    ) {
        static RecompenseView from(Recompense recompense) {
            return new RecompenseView(
                    recompense.getCode(),
                    recompense.getTitre(),
                    recompense.getDescription(),
                    recompense.getCout(),
                    recompense.getIcone(),
                    recompense.getCategorie());
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
            List<PalierView> paliers
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
                    views);
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
