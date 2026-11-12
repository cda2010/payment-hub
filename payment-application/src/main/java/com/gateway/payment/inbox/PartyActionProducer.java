package com.gateway.payment.inbox;

import com.gateway.payment.inbox.kafka.PartyActionEvent;
import com.gateway.payment.inbox.kafka.PartyActionPublisher;
import com.gateway.payment.security.jwt.KeycloakUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Producteurs métier → message Kafka {@code party.action.*} (librairie payment-inbox-kafka).
 */
@Component
public class PartyActionProducer {

    private static final Logger log = LoggerFactory.getLogger(PartyActionProducer.class);

    private final PartyActionPublisher actions;

    public PartyActionProducer(PartyActionPublisher actions) {
        this.actions = actions;
    }

    public void approvalPending(UUID partyId, String tenantId, String paymentId, String profileCode) {
        if (partyId == null || !StringUtils.hasText(paymentId)) {
            return;
        }
        open(partyId, tenantId, "APPROVAL_PENDING",
                StringUtils.hasText(profileCode)
                        ? "Votre opération attend une approbation (" + profileCode + ")"
                        : null,
                "payment",
                paymentId,
                null);
    }

    public void approvalClosed(UUID partyId, String paymentId) {
        if (partyId == null || !StringUtils.hasText(paymentId)) {
            return;
        }
        complete(partyId, "APPROVAL_PENDING", "payment", paymentId, "DONE");
    }

    public void documentsMissing(UUID partyId, String tenantId, String paymentId, String detail) {
        if (partyId == null) {
            return;
        }
        open(partyId, tenantId, "DOC_REQUIRED",
                StringUtils.hasText(detail) ? detail : null,
                "payment",
                paymentId,
                Instant.now().plusSeconds(7 * 24 * 3600));
    }

    public void firstOpComplete(UUID partyId) {
        if (partyId == null) {
            return;
        }
        complete(partyId, "FIRST_OP", "party", partyId.toString(), "DONE");
    }

    public void cashoutCodeActive(UUID partyId, String tenantId, String paymentId, String codeHint) {
        if (partyId == null) {
            return;
        }
        open(partyId, tenantId, "CASHOUT_CODE",
                StringUtils.hasText(codeHint) ? codeHint : null,
                "payment",
                paymentId,
                Instant.now().plusSeconds(2 * 24 * 3600));
    }

    public static UUID currentPartyId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof KeycloakUserDetails kud) {
            String raw = kud.getPartyId();
            if (StringUtils.hasText(raw)) {
                try {
                    return UUID.fromString(raw.trim());
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    public static UUID partyIdFromContext(Map<String, Object> context) {
        if (context == null) {
            return currentPartyId();
        }
        Object raw = context.get("partyId");
        if (raw == null) {
            raw = context.get("customerPartyId");
        }
        if (raw != null) {
            try {
                return UUID.fromString(String.valueOf(raw).trim());
            } catch (IllegalArgumentException ignored) {
                // fall through
            }
        }
        return currentPartyId();
    }

    private void open(
            UUID partyId,
            String tenantId,
            String actionType,
            String subtitle,
            String aggregateType,
            String aggregateId,
            Instant expiresAt
    ) {
        try {
            actions.publish(PartyActionEvent.open(
                    partyId, tenantId, actionType, aggregateType, aggregateId, subtitle, expiresAt));
        } catch (Exception e) {
            log.warn("Échec publication party.action.open {}: {}", actionType, e.getMessage());
        }
    }

    private void complete(
            UUID partyId, String actionType, String aggregateType, String aggregateId, String status
    ) {
        try {
            actions.publish(PartyActionEvent.complete(
                    partyId, null, actionType, aggregateType, aggregateId, status));
        } catch (Exception e) {
            log.debug("Échec publication party.action.complete {}: {}", actionType, e.getMessage());
        }
    }
}
