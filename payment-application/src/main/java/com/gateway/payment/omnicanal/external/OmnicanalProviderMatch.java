package com.gateway.payment.omnicanal.external;

import com.gateway.payment.configuration.api.dto.ProviderResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Relie un code demandé (code FSE ou libellé de rail) à un fournisseur omnicanal.
 */
public final class OmnicanalProviderMatch {

    private OmnicanalProviderMatch() {
    }

    public static Optional<ProviderResponse> best(String requested, List<ProviderResponse> providers) {
        if (requested == null || requested.isBlank() || providers == null || providers.isEmpty()) {
            return Optional.empty();
        }
        String raw = requested.trim();
        String compact = compact(raw);
        Optional<ProviderResponse> exact = providers.stream()
                .filter(provider -> equals(raw, provider.code())
                        || equals(raw, provider.codeFse())
                        || equals(raw, provider.codeExterne()))
                .findFirst();
        if (exact.isPresent()) {
            return exact;
        }
        List<ProviderResponse> sameName = providers.stream()
                .filter(provider -> compact.equals(compact(provider.libelle())))
                .toList();
        if (!sameName.isEmpty()) {
            return Optional.of(prefer(sameName));
        }
        List<ProviderResponse> prefix = providers.stream()
                .filter(provider -> compact(provider.libelle()).startsWith(compact))
                .toList();
        if (prefix.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(prefer(prefix));
    }

    private static ProviderResponse prefer(List<ProviderResponse> providers) {
        return providers.stream()
                .min(Comparator
                        .comparingInt((ProviderResponse provider) -> burkina(provider) ? 0 : 1)
                        .thenComparing(provider -> provider.codeFse() == null ? "" : provider.codeFse()))
                .orElseThrow();
    }

    private static boolean burkina(ProviderResponse provider) {
        String country = provider.countryCode();
        return country != null && country.toLowerCase(Locale.ROOT).contains("burkina");
    }

    private static boolean equals(String left, String right) {
        return right != null && left.equalsIgnoreCase(right.trim());
    }

    static String compact(String value) {
        if (value == null) {
            return "";
        }
        return value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }
}
