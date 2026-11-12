package com.gateway.payment.observability.metrics;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class BusinessAmountExtractor {

    private static final Set<String> AMOUNT_KEYS = Set.of(
            "montant", "montantop", "montantoperation", "amount", "mtt"
    );
    private static final Set<String> BALANCE_KEYS = Set.of(
            "solde", "balance", "soldedisponible", "soldecomptable"
    );

    private BusinessAmountExtractor() {
    }

    public static Optional<BigDecimal> extractTransferAmount(Map<String, ?> params, Map<String, ?> response) {
        Optional<BigDecimal> fromParams = findNumeric(params, AMOUNT_KEYS);
        if (fromParams.isPresent()) {
            return fromParams;
        }
        return findNumeric(response, AMOUNT_KEYS);
    }

    public static Optional<BigDecimal> extractBalance(Map<String, ?> response) {
        return findNumeric(response, BALANCE_KEYS);
    }

    private static Optional<BigDecimal> findNumeric(Map<?, ?> map, Set<String> keys) {
        if (map == null || map.isEmpty()) {
            return Optional.empty();
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            String key = normalize(String.valueOf(entry.getKey()));
            if (!keys.contains(key)) {
                continue;
            }
            Optional<BigDecimal> parsed = parse(entry.getValue());
            if (parsed.isPresent()) {
                return parsed;
            }
        }
        return Optional.empty();
    }

    private static String normalize(String key) {
        return key.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
    }

    static Optional<BigDecimal> parse(Object raw) {
        if (raw == null) {
            return Optional.empty();
        }
        if (raw instanceof BigDecimal bd) {
            return Optional.of(bd);
        }
        if (raw instanceof Number number) {
            return Optional.of(BigDecimal.valueOf(number.doubleValue()));
        }
        String text = String.valueOf(raw).trim().replace(" ", "").replace(",", ".");
        if (text.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new BigDecimal(text));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
