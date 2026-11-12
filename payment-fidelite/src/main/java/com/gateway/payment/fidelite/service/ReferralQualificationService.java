package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.Evenement;
import com.gateway.payment.fidelite.domain.Parrainage;
import com.gateway.payment.fidelite.domain.ParrainageStatut;
import com.gateway.payment.fidelite.domain.Programme;
import com.gateway.payment.fidelite.domain.ProgrammeStatut;
import com.gateway.payment.fidelite.repository.InboundEventRepository;
import com.gateway.payment.fidelite.repository.ParrainageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Le parrainage qualifié crédite le parrain et le filleul une seule fois.
 * La référence du ledger est le code, donc un second passage ne paie pas de nouveau.
 * Les transactions suivantes du filleul ne passent pas par ici.
 */
@Service
public class ReferralQualificationService {

    public static final String KYC_APPROVED = "CUSTOMER_KYC_APPROVED";
    public static final String PAYMENT_COMPLETED = "PAYMENT_COMPLETED";

    private final ParrainageRepository parrainages;
    private final InboundEventRepository inbox;
    private final LedgerService ledger;

    public ReferralQualificationService(
            ParrainageRepository parrainages,
            InboundEventRepository inbox,
            LedgerService ledger
    ) {
        this.parrainages = parrainages;
        this.inbox = inbox;
        this.ledger = ledger;
    }

    public boolean kycDejaRecu(String tenantId, UUID partyId) {
        if (tenantId == null || partyId == null) {
            return false;
        }
        return inbox.existsByTenantIdAndCustomerIdAndEventTypeIgnoreCase(
                tenantId, partyId.toString(), KYC_APPROVED);
    }

    @Transactional
    public void considere(String tenantId, Evenement event) {
        if (tenantId == null || event == null || event.eventType() == null) {
            return;
        }
        UUID filleulId = party(event.customerId());
        if (filleulId == null) {
            return;
        }
        String type = event.eventType().trim().toUpperCase(Locale.ROOT);
        if (!KYC_APPROVED.equals(type) && !PAYMENT_COMPLETED.equals(type)) {
            return;
        }
        for (Parrainage parrainage : parrainages.findByFilleulPartyIdAndStatut(filleulId, ParrainageStatut.ASSOCIE)) {
            avancer(tenantId, parrainage, event, PAYMENT_COMPLETED.equals(type));
        }
    }

    private void avancer(String tenantId, Parrainage parrainage, Evenement event, boolean paiement) {
        Programme programme = parrainage.getProgramme();
        if (programme == null
                || !tenantId.equals(programme.getTenantId())
                || programme.getStatut() != ProgrammeStatut.ACTIF
                || parrainage.getStatut() != ParrainageStatut.ASSOCIE) {
            return;
        }
        if (depasse(parrainage, programme)) {
            parrainage.setStatut(ParrainageStatut.EXPIRE);
            parrainages.save(parrainage);
            return;
        }
        if (paiement && eligible(programme, event) && parrainage.getEligibleEventId() == null) {
            parrainage.setEligibleEventId(event.eventId());
            parrainage.setEligibleAt(Instant.now());
        }
        if (KYC_APPROVED.equals(event.eventType().trim().toUpperCase(Locale.ROOT))) {
            parrainage.setKycApproved(true);
        }
        if (!pret(parrainage, programme)) {
            parrainages.save(parrainage);
            return;
        }
        if (plafond(parrainage, programme)) {
            parrainage.setStatut(ParrainageStatut.REJETE);
            parrainage.setRejectionReason("PLAFOND");
            parrainages.save(parrainage);
            return;
        }
        parrainage.setStatut(ParrainageStatut.QUALIFIE);
        parrainage.setQualifiedAt(Instant.now());
        parrainages.save(parrainage);
        String code = parrainage.getCode();
        ledger.crediter(
                programme.getTenantId(),
                programme.getId(),
                parrainage.getParrainPartyId(),
                programme.getPointsParrain(),
                "Parrainage",
                "PARRAINAGE:" + code + ":PARRAIN");
        ledger.crediter(
                programme.getTenantId(),
                programme.getId(),
                parrainage.getFilleulPartyId(),
                programme.getPointsFilleul(),
                "Bienvenue parrainage",
                "PARRAINAGE:" + code + ":FILLEUL");
    }

    private static boolean pret(Parrainage parrainage, Programme programme) {
        if (parrainage.getEligibleEventId() == null) {
            return false;
        }
        return !programme.isReferralKycRequired() || parrainage.isKycApproved();
    }

    private static boolean depasse(Parrainage parrainage, Programme programme) {
        int days = programme.getReferralQualificationDays();
        if (days <= 0 || parrainage.getUsedAt() == null) {
            return false;
        }
        return Instant.now().isAfter(parrainage.getUsedAt().plus(Duration.ofDays(days)));
    }

    private boolean plafond(Parrainage parrainage, Programme programme) {
        int max = Math.max(0, programme.getReferralMaxPerMonth());
        Instant debut = YearMonth.now(ZoneOffset.UTC).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        long deja = parrainages.countByProgramme_IdAndParrainPartyIdAndStatutAndQualifiedAtGreaterThanEqual(
                programme.getId(),
                parrainage.getParrainPartyId(),
                ParrainageStatut.QUALIFIE,
                debut);
        return deja >= max;
    }

    private static boolean eligible(Programme programme, Evenement event) {
        BigDecimal minimum = programme.getReferralMinAmount() == null
                ? BigDecimal.ZERO
                : programme.getReferralMinAmount();
        if (event.amount() == null || event.amount().compareTo(minimum) < 0) {
            return false;
        }
        String currency = programme.getReferralCurrency();
        if (currency != null && !currency.isBlank()) {
            if (event.currency() == null || !currency.equalsIgnoreCase(event.currency().trim())) {
                return false;
            }
        }
        return produitAutorise(programme.getReferralProducts(), event);
    }

    private static boolean produitAutorise(String raw, Evenement event) {
        if (raw == null || raw.isBlank()) {
            return true;
        }
        Set<String> allowed = Arrays.stream(raw.split(","))
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toSet());
        if (allowed.isEmpty()) {
            return false;
        }
        String product = text(event, "productCode");
        String service = text(event, "serviceType");
        return (product != null && allowed.contains(product.toUpperCase(Locale.ROOT)))
                || (service != null && allowed.contains(service.toUpperCase(Locale.ROOT)));
    }

    private static String text(Evenement event, String key) {
        if (event.attributes() == null || event.attributes().get(key) == null) {
            return null;
        }
        String value = event.attributes().get(key).toString().trim();
        return value.isEmpty() ? null : value;
    }

    private static UUID party(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(customerId.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
