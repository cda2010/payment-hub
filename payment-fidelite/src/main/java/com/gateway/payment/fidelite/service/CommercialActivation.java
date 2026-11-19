package com.gateway.payment.fidelite.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.fidelite.domain.Campagne;
import com.gateway.payment.fidelite.domain.Contribution;
import com.gateway.payment.fidelite.domain.Evenement;
import com.gateway.payment.fidelite.domain.InboundEvent;
import com.gateway.payment.fidelite.domain.Mission;
import com.gateway.payment.fidelite.domain.MissionProgres;
import com.gateway.payment.fidelite.domain.Palier;
import com.gateway.payment.fidelite.domain.Programme;
import com.gateway.payment.fidelite.domain.CatalogueRecompense;
import com.gateway.payment.fidelite.domain.ProgrammeCatalogueRecompense;
import com.gateway.payment.fidelite.domain.SegmentClient;
import com.gateway.payment.fidelite.repository.CampagneRepository;
import com.gateway.payment.fidelite.repository.CoalitionMembreRepository;
import com.gateway.payment.fidelite.repository.ContributionRepository;
import com.gateway.payment.fidelite.repository.InboundEventRepository;
import com.gateway.payment.fidelite.repository.MissionProgresRepository;
import com.gateway.payment.fidelite.repository.MissionRepository;
import com.gateway.payment.fidelite.repository.PalierRepository;
import com.gateway.payment.fidelite.repository.ProgrammeCatalogueRecompenseRepository;
import com.gateway.payment.fidelite.repository.SegmentClientRepository;
import com.gateway.payment.fidelite.service.RuleEngine.Outcome;
import com.gateway.payment.fidelite.service.SegmentCalcul.Activite;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Campagnes, fenêtres, plafonds, missions et segments appliqués à un événement déjà accepté. */
@Service
public class CommercialActivation {

    private final CampagneRepository campagnes;
    private final MissionRepository missions;
    private final MissionProgresRepository progres;
    private final SegmentClientRepository segments;
    private final InboundEventRepository inbox;
    private final PalierRepository paliers;
    private final ProgrammeCatalogueRecompenseRepository offres;
    private final ContributionRepository contributions;
    private final CoalitionMembreRepository membres;
    private final LedgerService ledger;
    private final PlafondService plafonds;
    private final NotificationService notifications;
    private final ObjectMapper json;

    public CommercialActivation(
            CampagneRepository campagnes,
            MissionRepository missions,
            MissionProgresRepository progres,
            SegmentClientRepository segments,
            InboundEventRepository inbox,
            PalierRepository paliers,
            ProgrammeCatalogueRecompenseRepository offres,
            ContributionRepository contributions,
            CoalitionMembreRepository membres,
            LedgerService ledger,
            PlafondService plafonds,
            NotificationService notifications,
            ObjectMapper json
    ) {
        this.campagnes = campagnes;
        this.missions = missions;
        this.progres = progres;
        this.segments = segments;
        this.inbox = inbox;
        this.paliers = paliers;
        this.offres = offres;
        this.contributions = contributions;
        this.membres = membres;
        this.ledger = ledger;
        this.plafonds = plafonds;
        this.notifications = notifications;
        this.json = json;
    }

    public record Contexte(Evenement event, String palier, long solde) {
    }

    private record Fait(UUID evenement, Activite activite, Evenement source) {
    }

    @Transactional(readOnly = true)
    public Contexte preparer(String tenantId, Programme programme, UUID partyId, Evenement event) {
        long solde = ledger.disponible(tenantId, programme.getId(), partyId);
        String palier = palierCourant(programme.getId(), solde);
        List<Activite> avant = faits(tenantId, event.customerId()).stream()
                .filter(fait -> event.eventId() == null || !event.eventId().equals(fait.evenement()))
                .map(Fait::activite)
                .toList();
        Map<String, Object> attributes = new LinkedHashMap<>();
        if (event.attributes() != null) {
            attributes.putAll(event.attributes());
        }
        if (palier != null) {
            attributes.put("palier", palier);
        }
        Set<String> codes = SegmentCalcul.calculer(avant, Instant.now());
        if (!codes.isEmpty()) {
            attributes.put("segments", String.join(",", codes));
        }
        return new Contexte(new Evenement(
                event.eventId(), event.eventType(), event.customerId(), event.amount(), event.currency(), attributes
        ), palier, solde);
    }

    @Transactional(readOnly = true)
    public Outcome fenetre(String tenantId, String definition, Evenement event) {
        List<Evenement> histoire = new ArrayList<>();
        for (Fait fait : faits(tenantId, event.customerId())) {
            histoire.add(dater(fait));
        }
        return FenetreCalcul.evaluer(definition, event, histoire);
    }

    @Transactional
    public long ajuster(Programme programme, UUID partyId, Evenement event, String definition, long base) {
        if (base <= 0) {
            return 0;
        }
        long total = base;
        Instant maintenant = Instant.now();
        Map<String, Campagne> gagnantes = new LinkedHashMap<>();
        for (Campagne campagne : campagnes.ouvertes(programme.getId(), maintenant)) {
            if (!correspond(campagne, event, maintenant)) {
                continue;
            }
            String groupe = campagne.getGroupe() == null || campagne.getGroupe().isBlank()
                    ? campagne.getId().toString()
                    : campagne.getGroupe().trim();
            gagnantes.putIfAbsent(groupe, campagne);
        }
        for (Campagne campagne : gagnantes.values()) {
            long extra = CampagneFiltre.bonus(base, campagne.getMultiplicateur(), campagne.getBonusFixe());
            long place = campagne.getBudgetPoints() == null
                    ? extra
                    : campagne.getBudgetPoints() - campagne.getPointsAttribues();
            long accorde = Math.min(extra, Math.max(0, place));
            if (accorde <= 0) {
                continue;
            }
            campagne.setPointsAttribues(campagne.getPointsAttribues() + accorde);
            campagnes.save(campagne);
            total += accorde;
            notifications.prevenir(
                    programme.getTenantId(), partyId, programme.getId(), "CAMPAGNE",
                    "CAMPAGNE:" + campagne.getId() + ":" + partyId, campagne.getNom());
            if (campagne.getFinanceurId() != null) {
                contribution(programme, campagne.getFinanceurId(), partyId, accorde,
                        "CAMPAGNE:" + campagne.getId() + ":" + event.eventId());
            }
        }
        return bornes(programme, partyId, definition, total);
    }

    @Transactional
    public void apresCredit(
            Programme programme,
            UUID partyId,
            Evenement event,
            long points,
            String reference,
            String palierAvant,
            long soldeAvant
    ) {
        if (points <= 0) {
            return;
        }
        long solde = soldeAvant + points;
        notifications.prevenir(
                programme.getTenantId(), partyId, programme.getId(), "POINTS_GAGNES", reference,
                points + " points crédités");
        String palier = palierCourant(programme.getId(), solde);
        if (palier != null && !palier.equals(palierAvant)) {
            notifications.prevenir(
                    programme.getTenantId(), partyId, programme.getId(), "PALIER",
                    "PALIER:" + programme.getId() + ":" + partyId + ":" + palier,
                    "Nouveau palier " + palier);
        }
        for (ProgrammeCatalogueRecompense offre : offres.findByProgramme_IdOrderByPositionAsc(programme.getId())) {
            CatalogueRecompense catalogue = offre.getCatalogue();
            if (catalogue.getCout() > soldeAvant && catalogue.getCout() <= solde) {
                notifications.prevenir(
                        programme.getTenantId(), partyId, programme.getId(), "RECOMPENSE",
                        "RECOMPENSE:" + catalogue.getCode() + ":" + partyId,
                        "Récompense disponible : " + catalogue.getTitre());
            }
        }
        UUID marchand = identifiant(attribut(event, "destinationUniteId"));
        if (marchand != null && membres.existsByProgramme_IdAndMarchandId(programme.getId(), marchand)) {
            contribution(programme, marchand, partyId, points, "GAIN:" + reference);
        }
    }

    @Transactional
    public void cloturer(String tenantId, Evenement event) {
        UUID partyId = identifiant(event.customerId());
        if (partyId == null) {
            return;
        }
        avancerMissions(tenantId, partyId, event);
        List<Activite> toutes = faits(tenantId, event.customerId()).stream().map(Fait::activite).toList();
        segments.deleteByTenantIdAndPartyId(tenantId, partyId);
        segments.flush();
        Instant maintenant = Instant.now();
        for (String code : SegmentCalcul.calculer(toutes, maintenant)) {
            SegmentClient row = new SegmentClient();
            row.setTenantId(tenantId);
            row.setPartyId(partyId);
            row.setCode(code);
            row.setCalculeAt(maintenant);
            segments.save(row);
        }
    }

    @Transactional(readOnly = true)
    public Set<String> codes(String tenantId, UUID partyId) {
        Set<String> codes = new LinkedHashSet<>();
        for (SegmentClient row : segments.findByTenantIdAndPartyId(tenantId, partyId)) {
            codes.add(row.getCode());
        }
        return codes;
    }

    public void usage(Programme programme, UUID marchandId, UUID partyId, long points, String reference) {
        if (marchandId == null || !membres.existsByProgramme_IdAndMarchandId(programme.getId(), marchandId)) {
            return;
        }
        contribution(programme, marchandId, partyId, points, "USAGE:" + reference);
    }

    private static Evenement dater(Fait fait) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        if (fait.source().attributes() != null) {
            attributes.putAll(fait.source().attributes());
        }
        if (fait.activite().quand() != null) {
            attributes.put("receivedAt", fait.activite().quand().toString());
        }
        Evenement source = fait.source();
        return new Evenement(source.eventId(), source.eventType(), source.customerId(),
                source.amount(), source.currency(), attributes);
    }

    private void avancerMissions(String tenantId, UUID partyId, Evenement event) {
        for (Mission mission : missions.findByTenantIdAndActifTrue(tenantId)) {
            if (mission.getProgramme() == null || !retient(mission, event)) {
                continue;
            }
            LocalDate debut = fenetre(mission.getFenetreJours());
            MissionProgres row = progres.findByMission_IdAndPartyIdAndFenetreDebut(mission.getId(), partyId, debut)
                    .orElseGet(() -> neuf(mission, partyId, debut));
            if (row.isAccomplie()) {
                continue;
            }
            if ("AMOUNT".equals(mission.getMetrique())) {
                row.setMontant(row.getMontant().add(event.amount() == null ? BigDecimal.ZERO : event.amount()));
            } else if ("DISTINCT".equals(mission.getMetrique())) {
                String service = attribut(event, "serviceType");
                if (service == null) {
                    progres.save(row);
                    continue;
                }
                Set<String> connus = new LinkedHashSet<>();
                if (row.getServices() != null && !row.getServices().isBlank()) {
                    for (String code : row.getServices().split(",")) {
                        if (!code.isBlank()) {
                            connus.add(code.trim().toUpperCase(Locale.ROOT));
                        }
                    }
                }
                connus.add(service.trim().toUpperCase(Locale.ROOT));
                row.setServices(String.join(",", connus));
                row.setCompteur(connus.size());
            } else {
                row.setCompteur(row.getCompteur() + 1);
            }
            boolean atteinte = "AMOUNT".equals(mission.getMetrique())
                    ? row.getMontant().compareTo(mission.getCible()) >= 0
                    : BigDecimal.valueOf(row.getCompteur()).compareTo(mission.getCible()) >= 0;
            if (atteinte) {
                row.setAccomplie(true);
                String reference = "MISSION:" + mission.getId() + ":" + partyId + ":" + debut;
                ledger.crediter(tenantId, mission.getProgramme().getId(), partyId,
                        mission.getPoints(), mission.getNom(), reference);
                notifications.prevenir(tenantId, partyId, mission.getProgramme().getId(), "MISSION",
                        reference, "Mission terminée : " + mission.getNom());
            }
            progres.save(row);
        }
    }

    private long bornes(Programme programme, UUID partyId, String definition, long total) {
        long jour = limite(definition, "maxPointsPerDay");
        long mois = limite(definition, "maxPointsPerMonth");
        long tout = limite(definition, "maxPointsPerProgramme");
        LocalDate aujourdhui = LocalDate.now(ZoneOffset.UTC);
        String moisCle = YearMonth.from(aujourdhui).toString();
        long accorde = total;
        if (jour > 0) {
            accorde = Math.min(accorde, Math.max(0, jour - plafonds.deja(
                    programme.getTenantId(), programme.getId(), partyId, "JOUR", aujourdhui.toString())));
        }
        if (mois > 0) {
            accorde = Math.min(accorde, Math.max(0, mois - plafonds.deja(
                    programme.getTenantId(), programme.getId(), partyId, "MOIS", moisCle)));
        }
        if (tout > 0) {
            accorde = Math.min(accorde, Math.max(0, tout - plafonds.deja(
                    programme.getTenantId(), programme.getId(), partyId, "PROGRAMME", "TOUT")));
        }
        if (accorde <= 0) {
            return 0;
        }
        if (jour > 0) {
            plafonds.ajouter(programme.getTenantId(), programme.getId(), partyId, "JOUR", aujourdhui.toString(), accorde);
        }
        if (mois > 0) {
            plafonds.ajouter(programme.getTenantId(), programme.getId(), partyId, "MOIS", moisCle, accorde);
        }
        if (tout > 0) {
            plafonds.ajouter(programme.getTenantId(), programme.getId(), partyId, "PROGRAMME", "TOUT", accorde);
        }
        return accorde;
    }

    private void contribution(Programme programme, UUID marchandId, UUID partyId, long points, String reference) {
        if (points <= 0 || contributions.existsByTenantIdAndBusinessReference(programme.getTenantId(), reference)) {
            return;
        }
        Contribution row = new Contribution();
        row.setTenantId(programme.getTenantId());
        row.setProgrammeId(programme.getId());
        row.setMarchandId(marchandId);
        row.setPartyId(partyId);
        row.setSens(reference.startsWith("USAGE:") ? "USAGE" : "EMISSION");
        row.setPoints(points);
        row.setBusinessReference(reference);
        row.setCreatedAt(Instant.now());
        contributions.save(row);
    }

    private boolean correspond(Campagne campagne, Evenement event, Instant maintenant) {
        return CampagneFiltre.correspond(
                campagne.isActif(), campagne.getDebut(), campagne.getFin(), campagne.isWeekendSeulement(),
                campagne.getProfils(), campagne.getRegions(), campagne.getUniteId(), campagne.getProduits(),
                campagne.getServices(), campagne.getCanaux(), campagne.getSegmentRequis(), event, maintenant);
    }

    private static boolean retient(Mission mission, Evenement event) {
        return egal(mission.getFiltreProduit(), attribut(event, "productCode"))
                && egal(mission.getFiltreService(), attribut(event, "serviceType"))
                && egal(mission.getFiltreCanal(), attribut(event, "channel"));
    }

    private static boolean egal(String attendu, String reel) {
        return attendu == null || attendu.isBlank() || attendu.equalsIgnoreCase(reel == null ? "" : reel);
    }

    private static LocalDate fenetre(int jours) {
        LocalDate aujourdhui = LocalDate.now(ZoneOffset.UTC);
        if (jours <= 0) {
            return LocalDate.of(1970, 1, 1);
        }
        if (jours == 30) {
            return YearMonth.from(aujourdhui).atDay(1);
        }
        long epoch = aujourdhui.toEpochDay();
        return LocalDate.ofEpochDay(epoch - Math.floorMod(epoch, jours));
    }

    private static MissionProgres neuf(Mission mission, UUID partyId, LocalDate debut) {
        MissionProgres created = new MissionProgres();
        created.setMission(mission);
        created.setPartyId(partyId);
        created.setFenetreDebut(debut);
        created.setCompteur(0);
        created.setMontant(BigDecimal.ZERO);
        created.setServices("");
        return created;
    }

    private String palierCourant(UUID programmeId, long solde) {
        String courant = null;
        for (Palier palier : paliers.findByProgramme_IdOrderBySeuilPointsAsc(programmeId)) {
            if (palier.getSeuilPoints() <= solde) {
                courant = palier.getCode();
            }
        }
        return courant;
    }

    private List<Fait> faits(String tenantId, String customerId) {
        if (tenantId == null || customerId == null) {
            return List.of();
        }
        List<Fait> rows = new java.util.ArrayList<>();
        for (InboundEvent recu : inbox.findByTenantIdAndCustomerIdOrderByReceivedAtAsc(tenantId, customerId)) {
            Evenement source = lire(recu.getPayload());
            if (source == null) {
                continue;
            }
            rows.add(new Fait(source.eventId(), new Activite(
                    recu.getReceivedAt(),
                    source.amount(),
                    attribut(source, "channel"),
                    attribut(source, "productCode"),
                    attribut(source, "serviceType")
            ), source));
        }
        return rows;
    }

    private Evenement lire(String payload) {
        try {
            return json.readValue(payload, Evenement.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static long limite(String definition, String nom) {
        if (definition == null || definition.isBlank()) {
            return 0;
        }
        try {
            JsonNode limits = new ObjectMapper().readTree(definition).get("limits");
            if (limits == null || !limits.hasNonNull(nom)) {
                return 0;
            }
            return limits.get(nom).asLong(0);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String attribut(Evenement event, String key) {
        if (event == null || event.attributes() == null || event.attributes().get(key) == null) {
            return null;
        }
        String value = event.attributes().get(key).toString().trim();
        return value.isEmpty() ? null : value;
    }

    private static UUID identifiant(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
