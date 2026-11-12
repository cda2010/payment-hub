package com.gateway.payment.observability.metrics;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ConfigurationProperties(prefix = "gateway.business-metrics")
public class BusinessMetricsProperties {

    private String defaultCategory = "AUTRE";
    private Map<String, List<String>> categories = new LinkedHashMap<>();
    private List<String> amountOperations = new ArrayList<>(List.of("TRANSFERT", "DEBIT", "CREDIT", "PAYEMENT"));
    private List<String> balanceOperations = new ArrayList<>(List.of("CONSULTATIONSOLDE", "CONSULTATION_SOLDE"));

    public String getDefaultCategory() {
        return defaultCategory;
    }

    public void setDefaultCategory(String defaultCategory) {
        this.defaultCategory = StringUtils.hasText(defaultCategory) ? defaultCategory.trim() : "AUTRE";
    }

    public Map<String, List<String>> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, List<String>> categories) {
        this.categories = categories != null ? categories : new LinkedHashMap<>();
    }

    public List<String> getAmountOperations() {
        return amountOperations;
    }

    public void setAmountOperations(List<String> amountOperations) {
        this.amountOperations = amountOperations != null ? amountOperations : new ArrayList<>();
    }

    public List<String> getBalanceOperations() {
        return balanceOperations;
    }

    public void setBalanceOperations(List<String> balanceOperations) {
        this.balanceOperations = balanceOperations != null ? balanceOperations : new ArrayList<>();
    }

    public boolean tracksAmount(String operation) {
        return containsIgnoreCase(amountOperations, operation);
    }

    public boolean tracksBalance(String operation) {
        return containsIgnoreCase(balanceOperations, operation);
    }

    private static boolean containsIgnoreCase(List<String> values, String candidate) {
        if (candidate == null || values == null) {
            return false;
        }
        String needle = candidate.trim().toUpperCase(Locale.ROOT);
        for (String value : values) {
            if (value != null && value.trim().toUpperCase(Locale.ROOT).equals(needle)) {
                return true;
            }
        }
        return false;
    }
}
