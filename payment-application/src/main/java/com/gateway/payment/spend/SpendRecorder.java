package com.gateway.payment.spend;

import com.gateway.payment.inbox.PartyActionProducer;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.spend.kafka.SpendEvent;
import com.gateway.payment.spend.kafka.SpendPublisher;
import com.gateway.payment.spend.kafka.UniteRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Au succès d'un paiement, publie le fait. payment-divers le classe.
 */
@Component
public class SpendRecorder {

    private static final Logger log = LoggerFactory.getLogger(SpendRecorder.class);

    private final SpendPublisher publisher;

    public SpendRecorder(SpendPublisher publisher) {
        this.publisher = publisher;
    }

    public void record(PaymentTransaction tx, Map<String, Object> context) {
        if (tx == null || tx.getPaymentId() == null) {
            return;
        }
        UUID partyId = PartyActionProducer.partyIdFromContext(context);
        if (partyId == null) {
            return;
        }
        try {
            publisher.publish(new SpendEvent(
                    tx.getPaymentId(),
                    partyId,
                    tx.getTenantId(),
                    tx.getAmount(),
                    tx.getCurrency(),
                    tx.getServiceType() == null ? null : tx.getServiceType().name(),
                    text(context, "catalogServiceCode"),
                    firstText(context, "categorieDepense", "billerCategory", "categoryCode"),
                    firstText(context, "merchantName", "merchant", "billerName"),
                    tx.getCompletedAt() != null ? tx.getCompletedAt() : Instant.now(),
                    text(context, "channel"),
                    firstText(context, "destinationUniteId", "agentUniteId"),
                    firstText(context, "destinationUniteType", "agentUniteType"),
                    lineage(context)));
        } catch (Exception e) {
            log.warn("Échec publication payment.spend {}: {}", tx.getPaymentId(), e.getMessage());
        }
    }

    private static List<UniteRef> lineage(Map<String, Object> context) {
        Object raw = context == null ? null : context.get("lignee");
        List<UniteRef> refs = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> row && row.get("id") != null) {
                    String id = String.valueOf(row.get("id")).trim();
                    if (!id.isEmpty()) {
                        Object type = row.get("type");
                        refs.add(new UniteRef(id, type == null ? null : String.valueOf(type).trim()));
                    }
                }
            }
        }
        if (!refs.isEmpty()) {
            return refs;
        }
        String id = firstText(context, "destinationUniteId", "agentUniteId");
        if (id == null) {
            return List.of();
        }
        return List.of(new UniteRef(id, firstText(context, "destinationUniteType", "agentUniteType")));
    }

    private static String firstText(Map<String, Object> context, String... keys) {
        for (String key : keys) {
            String value = text(context, key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String text(Map<String, Object> context, String key) {
        if (context == null || context.get(key) == null) {
            return null;
        }
        String value = String.valueOf(context.get(key)).trim();
        return value.isEmpty() ? null : value;
    }
}
