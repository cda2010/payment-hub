package com.gateway.payment.fidelite.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.fidelite.domain.Adhesion;
import com.gateway.payment.fidelite.domain.Evenement;
import com.gateway.payment.fidelite.domain.InboundEvent;
import com.gateway.payment.fidelite.domain.LoyaltyRule;
import com.gateway.payment.fidelite.domain.LoyaltyRuleVersion;
import com.gateway.payment.fidelite.domain.Programme;
import com.gateway.payment.fidelite.domain.ProgrammeStatut;
import com.gateway.payment.fidelite.domain.Tenant;
import com.gateway.payment.fidelite.repository.AdhesionRepository;
import com.gateway.payment.fidelite.repository.InboundEventRepository;
import com.gateway.payment.fidelite.repository.LoyaltyRuleRepository;
import com.gateway.payment.fidelite.repository.LoyaltyRuleVersionRepository;
import com.gateway.payment.fidelite.repository.ProgrammeRepository;
import com.gateway.payment.fidelite.repository.TenantRepository;
import com.gateway.payment.fidelite.service.RuleEngine.Outcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

/**
 * Reçoit un événement, le dédoublonne, puis crédite les programmes actifs du client.
 * La réponse 202 ne contient pas les points : accepté ne veut pas dire attribué.
 */
@Service
public class AttributionService {

    private final TenantRepository tenants;
    private final InboundEventRepository inbox;
    private final AdhesionRepository adhesions;
    private final ProgrammeRepository programmes;
    private final LoyaltyRuleRepository rules;
    private final LoyaltyRuleVersionRepository versions;
    private final LedgerService ledger;
    private final RuleEngine engine;
    private final ObjectMapper json;
    private final ReferralQualificationService referrals;
    private final CommercialActivation commercial;

    public AttributionService(
            TenantRepository tenants,
            InboundEventRepository inbox,
            AdhesionRepository adhesions,
            ProgrammeRepository programmes,
            LoyaltyRuleRepository rules,
            LoyaltyRuleVersionRepository versions,
            LedgerService ledger,
            RuleEngine engine,
            ObjectMapper json,
            ReferralQualificationService referrals
    ) {
        this(tenants, inbox, adhesions, programmes, rules, versions, ledger, engine, json, referrals, null);
    }

    @Autowired
    public AttributionService(
            TenantRepository tenants,
            InboundEventRepository inbox,
            AdhesionRepository adhesions,
            ProgrammeRepository programmes,
            LoyaltyRuleRepository rules,
            LoyaltyRuleVersionRepository versions,
            LedgerService ledger,
            RuleEngine engine,
            ObjectMapper json,
            ReferralQualificationService referrals,
            CommercialActivation commercial
    ) {
        this.tenants = tenants;
        this.inbox = inbox;
        this.adhesions = adhesions;
        this.programmes = programmes;
        this.rules = rules;
        this.versions = versions;
        this.ledger = ledger;
        this.engine = engine;
        this.json = json;
        this.referrals = referrals;
        this.commercial = commercial;
    }

    /** Évalue une définition sans la publier et sans toucher le ledger. */
    public Outcome simuler(String definition, Evenement event) {
        if (definition == null || definition.isBlank()) {
            throw new IllegalArgumentException("La définition de la règle est requise");
        }
        if (event == null) {
            throw new IllegalArgumentException("L'événement est requis");
        }
        return engine.evaluate(definition, event);
    }

    @Transactional
    public ReglePubliee publier(String tenantId, UUID programmeId, String code, int priority, String definition) {
        return publier(tenantId, programmeId, code, priority, definition, null, null);
    }

    @Transactional
    public ReglePubliee publier(
            String tenantId, UUID programmeId, String code, int priority, String definition,
            Instant debut, Instant fin
    ) {
        Tenant tenant = actif(tenantId);
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Le code de la règle est requis");
        }
        if (definition == null || definition.isBlank()) {
            throw new IllegalArgumentException("La définition de la règle est requise");
        }
        Programme programme = programmes.findById(programmeId)
                .orElseThrow(() -> new NoSuchElementException("Programme introuvable"));
        if (!tenant.getCode().equals(programme.getTenantId())) {
            throw new IllegalArgumentException("Le programme n'appartient pas à ce tenant");
        }
        String normalized = code.trim().toUpperCase();
        LoyaltyRule rule = rules.findByTenantIdAndCode(tenant.getCode(), normalized).orElseGet(() -> {
            LoyaltyRule created = new LoyaltyRule();
            created.setTenantId(tenant.getCode());
            created.setProgramme(programme);
            created.setCode(normalized);
            created.setStatut("BROUILLON");
            return rules.save(created);
        });
        if (!rule.getProgramme().getId().equals(programmeId)) {
            throw new IllegalArgumentException("Ce code de règle appartient à un autre programme");
        }
        LoyaltyRuleVersion version = new LoyaltyRuleVersion();
        version.setRule(rule);
        version.setVersionNo(versions.latest(rule.getId()) + 1);
        version.setPriority(priority);
        version.setDefinition(definition);
        version.setStartsAt(debut);
        version.setEndsAt(fin);
        version.setPublishedAt(Instant.now());
        LoyaltyRuleVersion saved = versions.save(version);
        rule.setStatut("PUBLIE");
        rules.save(rule);
        return new ReglePubliee(rule.getCode(), saved.getVersionNo(), saved.getId());
    }

    @Transactional
    public Accuse recevoir(String tenantId, Evenement event) {
        Tenant tenant = actif(tenantId);
        if (event == null || event.eventId() == null) {
            throw new IllegalArgumentException("L'identifiant de l'événement est requis");
        }
        if (event.eventType() == null || event.eventType().isBlank()) {
            throw new IllegalArgumentException("Le type d'événement est requis");
        }
        if (event.customerId() == null || event.customerId().isBlank()) {
            throw new IllegalArgumentException("Le client est requis");
        }
        InboundEvent existing = inbox.findByTenantIdAndEventId(tenant.getCode(), event.eventId()).orElse(null);
        if (existing != null) {
            return new Accuse(existing.getId().toString(), "ACCEPTED");
        }
        InboundEvent received = new InboundEvent();
        received.setTenantId(tenant.getCode());
        received.setEventId(event.eventId());
        received.setEventType(event.eventType().trim());
        received.setCustomerId(event.customerId().trim());
        received.setPayload(payload(event));
        received.setStatus("PROCESSED");
        received.setReceivedAt(Instant.now());
        received = inbox.save(received);
        attribuer(tenant.getCode(), event);
        return new Accuse(received.getId().toString(), "ACCEPTED");
    }

    private void attribuer(String tenantId, Evenement event) {
        UUID partyId = party(event.customerId());
        if (partyId == null) {
            return;
        }
        for (Adhesion adhesion : adhesions.findByPartyId(partyId)) {
            Programme programme = adhesion.getProgramme();
            if (programme == null
                    || !tenantId.equals(programme.getTenantId())
                    || programme.getStatut() != ProgrammeStatut.ACTIF) {
                continue;
            }
            CommercialActivation.Contexte contexte = commercial == null
                    ? null
                    : commercial.preparer(tenantId, programme, partyId, event);
            Evenement courant = contexte == null ? event : contexte.event();
            List<LoyaltyRuleVersion> applicables = new ArrayList<>();
            List<Outcome> gains = new ArrayList<>();
            Set<String> groupes = new HashSet<>();
            for (LoyaltyRuleVersion version : versions.published(tenantId, programme.getId())) {
                if (horsCalendrier(version)) {
                    continue;
                }
                Outcome outcome = engine.evaluate(version.getDefinition(), courant);
                if (!outcome.applicable() && "Fenetre".equals(outcome.motif()) && commercial != null) {
                    outcome = commercial.fenetre(tenantId, version.getDefinition(), courant);
                }
                if (!outcome.applicable() || outcome.points() <= 0) {
                    continue;
                }
                String groupe = groupe(version.getDefinition());
                if (groupe != null && !groupes.add(groupe)) {
                    continue;
                }
                applicables.add(version);
                gains.add(outcome);
            }
            boolean exclusive = gains.stream().anyMatch(Outcome::exclusive);
            for (int index = 0; index < applicables.size(); index++) {
                if (exclusive && !gains.get(index).exclusive()) {
                    continue;
                }
                if (exclusive && gains.subList(0, index).stream().anyMatch(Outcome::exclusive)) {
                    continue;
                }
                LoyaltyRuleVersion version = applicables.get(index);
                Outcome gain = gains.get(index);
                String reference = event.eventId() + ":" + version.getId();
                if (gain.motif() != null && gain.motif().startsWith("REF:")) {
                    reference = version.getId() + ":" + gain.motif().substring(4);
                }
                if (ledger.dejaEcrit(tenantId, reference)) {
                    continue;
                }
                long points = gain.points();
                if (commercial != null) {
                    points = commercial.ajuster(programme, partyId, courant, version.getDefinition(), points);
                }
                if (points <= 0) {
                    continue;
                }
                ledger.crediter(
                        tenantId,
                        programme.getId(),
                        partyId,
                        points,
                        version.getRule().getCode(),
                        reference);
                if (commercial != null && contexte != null) {
                    commercial.apresCredit(
                            programme, partyId, courant, points, reference, contexte.palier(), contexte.solde());
                }
            }
        }
        referrals.considere(tenantId, event);
        if (commercial != null) {
            commercial.cloturer(tenantId, event);
        }
    }

    /** Évalue une définition sur les événements déjà reçus. N'écrit ni règle ni mouvement. */
    @Transactional(readOnly = true)
    public List<Replay> rejouer(String tenantId, String definition, Instant depuis) {
        Tenant tenant = actif(tenantId);
        Instant start = depuis == null ? Instant.EPOCH : depuis;
        List<Replay> rows = new ArrayList<>();
        List<Evenement> histoire = new ArrayList<>();
        for (InboundEvent recu : inbox.findByTenantIdAndReceivedAtGreaterThanEqualOrderByReceivedAtAsc(tenant.getCode(), start)) {
            Evenement event = lire(recu.getPayload());
            if (event == null) {
                continue;
            }
            java.util.Map<String, Object> attributes = new LinkedHashMap<>();
            if (event.attributes() != null) {
                attributes.putAll(event.attributes());
            }
            if (recu.getReceivedAt() != null) {
                attributes.put("receivedAt", recu.getReceivedAt().toString());
            }
            event = new Evenement(event.eventId(), event.eventType(), event.customerId(),
                    event.amount(), event.currency(), attributes);
            histoire.add(event);
            Outcome outcome = engine.evaluate(definition, event);
            if (!outcome.applicable() && "Fenetre".equals(outcome.motif())) {
                outcome = FenetreCalcul.evaluer(definition, event, histoire);
            }
            rows.add(new Replay(recu.getEventId(), outcome.applicable(), outcome.points(), outcome.motif()));
        }
        return rows;
    }

    public record Replay(UUID eventId, boolean applicable, long points, String motif) {
    }

    private Evenement lire(String payload) {
        try {
            return json.readValue(payload, Evenement.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean horsCalendrier(LoyaltyRuleVersion version) {
        Instant maintenant = Instant.now();
        if (version.getStartsAt() != null && maintenant.isBefore(version.getStartsAt())) {
            return true;
        }
        return version.getEndsAt() != null && !maintenant.isBefore(version.getEndsAt());
    }

    private String groupe(String definition) {
        try {
            JsonNode node = json.readTree(definition).get("groupe");
            if (node == null || node.isNull()) {
                return null;
            }
            String value = node.asText().trim();
            return value.isEmpty() ? null : value;
        } catch (Exception e) {
            return null;
        }
    }

    private Tenant actif(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Le tenant est requis");
        }
        Tenant tenant = tenants.findByCode(tenantId.trim())
                .orElseThrow(() -> new IllegalArgumentException("Tenant introuvable"));
        if (!"ACTIF".equals(tenant.getStatus())) {
            throw new IllegalArgumentException("Le tenant est suspendu");
        }
        return tenant;
    }

    private String payload(Evenement event) {
        try {
            return json.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Événement illisible");
        }
    }

    private static UUID party(String customerId) {
        try {
            return UUID.fromString(customerId.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public record Accuse(String trackingId, String status) {
    }

    public record ReglePubliee(String code, int version, UUID versionId) {
    }
}
