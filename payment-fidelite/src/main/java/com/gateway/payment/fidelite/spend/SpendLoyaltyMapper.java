package com.gateway.payment.fidelite.spend;

import com.gateway.payment.fidelite.domain.Evenement;
import com.gateway.payment.spend.kafka.SpendEvent;
import com.gateway.payment.spend.kafka.UniteRef;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Traduit un paiement réussi en événement de fidélité. Le paymentId devient l'identifiant stable. */
public final class SpendLoyaltyMapper {

    public static final String EVENT_TYPE = "PAYMENT_COMPLETED";

    private SpendLoyaltyMapper() {
    }

    public static Evenement toEvenement(SpendEvent spend) {
        if (spend == null || spend.partyId() == null || spend.paymentId() == null || spend.paymentId().isBlank()) {
            return null;
        }
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, "channel", spend.channel());
        put(attributes, "serviceType", spend.serviceType());
        put(attributes, "productCode", spend.productCode());
        put(attributes, "merchantName", spend.merchantName());
        put(attributes, "categorieHint", spend.categorieHint());
        put(attributes, "destinationUniteId", spend.destinationUniteId());
        put(attributes, "destinationUniteType", spend.destinationUniteType());
        List<Map<String, String>> lineage = lineage(spend.lignee());
        if (!lineage.isEmpty()) {
            attributes.put("lignee", lineage);
        }
        String currency = spend.currency() == null || spend.currency().isBlank() ? "XOF" : spend.currency().trim();
        return new Evenement(
                eventId(spend.paymentId()),
                EVENT_TYPE,
                spend.partyId().toString(),
                spend.amount(),
                currency,
                attributes);
    }

    public static String tenant(SpendEvent spend) {
        if (spend == null || spend.tenantId() == null || spend.tenantId().isBlank()) {
            return "CORIS_DEMO";
        }
        return spend.tenantId().trim();
    }

    public static UUID eventId(String paymentId) {
        String value = paymentId.trim();
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return UUID.nameUUIDFromBytes(("payment.spend:" + value).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static List<Map<String, String>> lineage(List<UniteRef> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }
        List<Map<String, String>> rows = new ArrayList<>();
        for (UniteRef node : nodes) {
            if (node == null || node.id() == null || node.id().isBlank()) {
                continue;
            }
            Map<String, String> row = new LinkedHashMap<>();
            row.put("id", node.id().trim());
            if (node.type() != null && !node.type().isBlank()) {
                row.put("type", node.type().trim());
            }
            rows.add(row);
        }
        return rows;
    }

    private static void put(Map<String, Object> attributes, String key, String value) {
        if (value != null && !value.isBlank()) {
            attributes.put(key, value.trim());
        }
    }
}
