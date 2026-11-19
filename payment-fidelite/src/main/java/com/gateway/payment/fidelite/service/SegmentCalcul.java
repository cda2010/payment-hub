package com.gateway.payment.fidelite.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Segments lus sur l'historique. Le seuil de forte valeur est 100 000 XOF sur 30 jours. */
public final class SegmentCalcul {

    public static final BigDecimal SEUIL_FORTE_VALEUR = new BigDecimal("100000");

    public record Activite(Instant quand, BigDecimal montant, String canal, String produit, String service) {
    }

    private SegmentCalcul() {
    }

    public static Set<String> calculer(List<Activite> activites, Instant maintenant) {
        Set<String> codes = new HashSet<>();
        if (activites == null || activites.isEmpty() || maintenant == null) {
            return codes;
        }
        Instant premier = null;
        Instant dernier = null;
        BigDecimal recent = BigDecimal.ZERO;
        boolean qr = false;
        boolean fenetrePrecedente = false;
        Instant moins30 = maintenant.minus(30, ChronoUnit.DAYS);
        Instant moins60 = maintenant.minus(60, ChronoUnit.DAYS);
        for (Activite activite : activites) {
            if (activite == null || activite.quand() == null || activite.quand().isAfter(maintenant)) {
                continue;
            }
            if (premier == null || activite.quand().isBefore(premier)) {
                premier = activite.quand();
            }
            if (dernier == null || activite.quand().isAfter(dernier)) {
                dernier = activite.quand();
            }
            if (!activite.quand().isBefore(moins30)) {
                recent = recent.add(activite.montant() == null ? BigDecimal.ZERO : activite.montant());
            } else if (!activite.quand().isBefore(moins60)) {
                fenetrePrecedente = true;
            }
            if (contientQr(activite.canal()) || contientQr(activite.produit()) || contientQr(activite.service())) {
                qr = true;
            }
        }
        if (premier == null || dernier == null) {
            return codes;
        }
        if (!premier.isBefore(moins30)) {
            codes.add("NOUVEAU");
        }
        if (!dernier.isBefore(moins30)) {
            codes.add("ACTIF");
        } else {
            codes.add("INACTIF_30");
            if (dernier.isBefore(moins60)) {
                codes.add("INACTIF_60");
            }
        }
        if (fenetrePrecedente && dernier.isBefore(moins30)) {
            codes.add("ATTRITION");
        }
        if (recent.compareTo(SEUIL_FORTE_VALEUR) >= 0) {
            codes.add("FORTE_VALEUR");
        }
        if (qr) {
            codes.add("QR");
        }
        return codes;
    }

    private static boolean contientQr(String value) {
        return value != null && value.toUpperCase(Locale.ROOT).contains("QR");
    }
}
