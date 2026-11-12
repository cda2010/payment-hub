package com.gateway.payment.fidelite.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
            List<LoyaltyRuleVersion> applicables = new ArrayList<>();
            List<Outcome> gains = new ArrayList<>();
            for (LoyaltyRuleVersion version : versions.published(tenantId, programme.getId())) {
                Outcome outcome = engine.evaluate(version.getDefinition(), event);
                if (!outcome.applicable() || outcome.points() <= 0) {
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
                ledger.crediter(
                        tenantId,
                        programme.getId(),
                        partyId,
                        gains.get(index).points(),
                        version.getRule().getCode(),
                        event.eventId() + ":" + version.getId());
            }
        }
        referrals.considere(tenantId, event);
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
