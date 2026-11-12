package com.gateway.payment.observability.metrics;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BusinessOperationClassifier {

    private static final Logger log = LoggerFactory.getLogger(BusinessOperationClassifier.class);

    private final BusinessMetricsProperties properties;
    private final Map<String, String> typeToCategory = new ConcurrentHashMap<>();

    public BusinessOperationClassifier(BusinessMetricsProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        rebuildIndex();
    }

    public void rebuildIndex() {
        typeToCategory.clear();
        Map<String, List<String>> configured = properties.getCategories();
        if (configured == null || configured.isEmpty()) {
            configured = defaultCategories();
        }
        for (Map.Entry<String, List<String>> entry : configured.entrySet()) {
            String category = normalize(entry.getKey());
            if (!StringUtils.hasText(category) || entry.getValue() == null) {
                continue;
            }
            for (String type : entry.getValue()) {
                if (StringUtils.hasText(type)) {
                    typeToCategory.put(normalize(type), category);
                }
            }
        }
        log.info("Business classifier — {} types, amountOps={}, balanceOps={}",
                typeToCategory.size(), properties.getAmountOperations(), properties.getBalanceOperations());
    }

    public String operationOf(String typeMethode, String operationCodeFallback) {
        if (StringUtils.hasText(typeMethode)) {
            return normalize(typeMethode);
        }
        if (StringUtils.hasText(operationCodeFallback)) {
            return normalize(operationCodeFallback);
        }
        return "UNKNOWN";
    }

    public String categoryOf(String operation) {
        if (!StringUtils.hasText(operation)) {
            return normalize(properties.getDefaultCategory());
        }
        String mapped = typeToCategory.get(normalize(operation));
        return mapped != null ? mapped : normalize(properties.getDefaultCategory());
    }

    public boolean tracksAmount(String operation) {
        return properties.tracksAmount(operation);
    }

    public boolean tracksBalance(String operation) {
        return properties.tracksBalance(operation);
    }

    public Map<String, String> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(typeToCategory));
    }

    private static String normalize(String value) {
        return value == null ? "AUTRE" : value.trim().toUpperCase(Locale.ROOT);
    }

    static Map<String, List<String>> defaultCategories() {
        Map<String, List<String>> defaults = new LinkedHashMap<>();
        defaults.put("SOLDE", List.of("CONSULTATIONSOLDE", "CONSULTATION_SOLDE"));
        defaults.put("TRANSFERT", List.of("TRANSFERT", "DEBIT", "CREDIT"));
        defaults.put("HISTORIQUE", List.of("CONSULTATIONHIST", "CARTEMOUVEMENT", "LISTEVENEMENT"));
        defaults.put("INFOS", List.of("CONSULTATION", "INFOCLIENT", "GETINFOS"));
        defaults.put("FACTURE", List.of("GETMONTANT", "CONSULTATIONFACTURE", "VERIFREGLEFACTURIER", "PAIEMENT_FACTURE"));
        defaults.put("PAYMENT", List.of("PAYEMENT", "PAYMENT", "CHECKOUT"));
        return defaults;
    }
}
