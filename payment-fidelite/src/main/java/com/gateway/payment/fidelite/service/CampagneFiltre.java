package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.Evenement;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Décide si une campagne s'applique à un événement. N'écrit rien. */
public final class CampagneFiltre {

    private CampagneFiltre() {
    }

    public static boolean correspond(
            boolean actif,
            Instant debut,
            Instant fin,
            boolean weekendSeulement,
            String profils,
            String regions,
            UUID uniteId,
            String produits,
            String services,
            String canaux,
            String segmentRequis,
            Evenement event,
            Instant maintenant
    ) {
        if (!actif || event == null || maintenant == null || debut == null || fin == null) {
            return false;
        }
        if (maintenant.isBefore(debut) || !maintenant.isBefore(fin)) {
            return false;
        }
        if (weekendSeulement) {
            DayOfWeek jour = maintenant.atZone(ZoneOffset.UTC).getDayOfWeek();
            if (jour != DayOfWeek.SATURDAY && jour != DayOfWeek.SUNDAY) {
                return false;
            }
        }
        return contient(produits, attribut(event, "productCode"))
                && contient(services, attribut(event, "serviceType"))
                && contient(canaux, attribut(event, "channel"))
                && contient(regions, premier(event, "region", "ville", "city"))
                && profil(profils, event)
                && segment(segmentRequis, event)
                && unite(uniteId, event);
    }

    private static boolean profil(String filtre, Evenement event) {
        Set<String> allowed = split(filtre);
        if (allowed.isEmpty()) {
            return true;
        }
        Object raw = event.attributes() == null ? null : event.attributes().get("profils");
        if (raw instanceof Collection<?> values) {
            return values.stream().anyMatch(value -> allowed.contains(String.valueOf(value).trim().toUpperCase(Locale.ROOT)));
        }
        String one = premier(event, "profil", "profile");
        return one != null && allowed.contains(one.toUpperCase(Locale.ROOT));
    }

    private static boolean segment(String requis, Evenement event) {
        if (requis == null || requis.isBlank()) {
            return true;
        }
        String raw = attribut(event, "segments");
        if (raw == null) {
            return false;
        }
        for (String code : raw.split(",")) {
            if (requis.equalsIgnoreCase(code.trim())) {
                return true;
            }
        }
        return false;
    }

    private static boolean unite(UUID uniteId, Evenement event) {
        if (uniteId == null) {
            return true;
        }
        String id = uniteId.toString();
        if (id.equalsIgnoreCase(attribut(event, "destinationUniteId"))) {
            return true;
        }
        Object lineage = event.attributes() == null ? null : event.attributes().get("lignee");
        if (!(lineage instanceof List<?> nodes)) {
            return false;
        }
        for (Object node : nodes) {
            if (node instanceof Map<?, ?> row && id.equalsIgnoreCase(String.valueOf(row.get("id")).trim())) {
                return true;
            }
        }
        return false;
    }

    private static boolean contient(String filtre, String valeur) {
        Set<String> allowed = split(filtre);
        if (allowed.isEmpty()) {
            return true;
        }
        return valeur != null && allowed.contains(valeur.trim().toUpperCase(Locale.ROOT));
    }

    private static Set<String> split(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        return java.util.Arrays.stream(raw.split(","))
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .filter(value -> !value.isEmpty())
                .collect(java.util.stream.Collectors.toSet());
    }

    private static String premier(Evenement event, String... keys) {
        for (String key : keys) {
            String value = attribut(event, key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String attribut(Evenement event, String key) {
        if (event.attributes() == null || event.attributes().get(key) == null) {
            return null;
        }
        String value = event.attributes().get(key).toString().trim();
        return value.isEmpty() ? null : value;
    }

    public static long bonus(long base, BigDecimal multiplicateur, long bonusFixe) {
        BigDecimal facteur = multiplicateur == null || multiplicateur.signum() <= 0 ? BigDecimal.ONE : multiplicateur;
        long majore = BigDecimal.valueOf(Math.max(0, base)).multiply(facteur).setScale(0, java.math.RoundingMode.FLOOR).longValue();
        return Math.max(0, majore - Math.max(0, base)) + Math.max(0, bonusFixe);
    }
}
