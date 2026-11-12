package com.gateway.payment.billing.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Catégories de navigation facturiers (mobile + admin).
 * Code persisté sur {@link Biller#getCategoryCode()}.
 */
public enum BillerCategory {
    ENERGY_WATER("Énergie & eau", 1),
    TELECOM_MEDIA("Télécoms & TV", 2),
    EDUCATION("Éducation", 3),
    GOVERNMENT("État & taxes", 4),
    INSURANCE_HEALTH("Assurance & santé", 5),
    OTHER("Autres", 99);

    private final String label;
    private final int sortOrder;

    BillerCategory(String label, int sortOrder) {
        this.label = label;
        this.sortOrder = sortOrder;
    }

    public String getLabel() {
        return label;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public static Optional<BillerCategory> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        return Arrays.stream(values())
                .filter(c -> c.name().equals(normalized))
                .findFirst();
    }

    public static BillerCategory orOther(String code) {
        return fromCode(code).orElse(OTHER);
    }
}
