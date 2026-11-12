package com.gateway.payment.divers.spend;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/** Mois civil UTC. Absent ou illisible = mois courant. */
public record SpendMonth(YearMonth month) {

    public static SpendMonth parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return new SpendMonth(YearMonth.now(ZoneOffset.UTC));
        }
        try {
            return new SpendMonth(YearMonth.parse(raw.trim()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Mois attendu : AAAA-MM");
        }
    }

    public Instant start() {
        return month.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public Instant end() {
        return month.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public SpendMonth previous() {
        return new SpendMonth(month.minusMonths(1));
    }

    @Override
    public String toString() {
        return month.toString();
    }
}
