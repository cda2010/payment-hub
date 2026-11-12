package com.gateway.payment.pricing.api.dto;

import com.gateway.payment.pricing.domain.*;
import com.gateway.payment.pricing.tarif.moteur.Localisation;

import java.math.BigDecimal;
import java.util.List;

public final class PricingDtos {
    private PricingDtos() {}

    public record QuoteRequest(
            String productCode,
            String channel,
            String audience,
            BigDecimal amount,
            String currency,
            String fromCurrency,
            String toCurrency,
            String countryCode,
            String railCode,
            /** Source et destination de l'opération (unité, pays ou zone), pour les grilles qui les ciblent. */
            Localisation source,
            Localisation destination,
            /**
             * Override d'assiette ({@code CUSTOM} ou forçage parcours).
             * Si null, la grille résout via {@code baseCalcul} (montant, FX, cumul…).
             */
            BigDecimal feeBaseAmount,
            /** Agrégats pour cumuls : {@code cumulAmount}, {@code cumulAmount.MONTH.AGENT}, {@code cumulCount}… */
            java.util.Map<String, BigDecimal> aggregates
    ) {
        public QuoteRequest(String productCode, String channel, String audience, BigDecimal amount, String currency,
                            String fromCurrency, String toCurrency, String countryCode, String railCode) {
            this(productCode, channel, audience, amount, currency, fromCurrency, toCurrency, countryCode, railCode,
                    null, null, null, null);
        }

        public QuoteRequest(String productCode, String channel, String audience, BigDecimal amount, String currency,
                            String fromCurrency, String toCurrency, String countryCode, String railCode,
                            Localisation source, Localisation destination) {
            this(productCode, channel, audience, amount, currency, fromCurrency, toCurrency, countryCode, railCode,
                    source, destination, null, null);
        }

        public QuoteRequest(String productCode, String channel, String audience, BigDecimal amount, String currency,
                            String fromCurrency, String toCurrency, String countryCode, String railCode,
                            Localisation source, Localisation destination, BigDecimal feeBaseAmount) {
            this(productCode, channel, audience, amount, currency, fromCurrency, toCurrency, countryCode, railCode,
                    source, destination, feeBaseAmount, null);
        }
    }

    public record QuoteResponse(
            boolean allowed,
            List<String> violations,
            BigDecimal principal,
            BigDecimal feeAmount,
            BigDecimal convertedAmount,
            BigDecimal totalDebit,
            BigDecimal amountReceived,
            BigDecimal appliedRate,
            /** Grille tarifaire et formule ayant produit les frais (vides si aucune grille ne s'applique). */
            String feeGridCode,
            Integer feeGridVersion,
            String formulaCode,
            String rateCode,
            String policyCode,
            String feeBearer,
            String feeBreakdown,
            String snapshotId,
            /** Assiette réellement utilisée pour le calcul des frais. */
            BigDecimal feeBaseAmount,
            String baseCalcul,
            String cumulPeriod,
            String cumulScope
    ) {}

    public record RateDto(
            String code, RateKind rateKind, String baseCurrency, String quoteCurrency,
            BigDecimal rate, boolean active
    ) {
        public static RateDto from(PricingRate r) {
            return new RateDto(r.getCode(), r.getRateKind(), r.getBaseCurrency(),
                    r.getQuoteCurrency(), r.getRate(), r.isActive());
        }
    }

    public record PolicyDto(
            String code, String name, String productCode, String channel, String audience,
            String countryCode, String railCode, PricingFeeBearer feeBearer, String rateCode,
            int priority, boolean active
    ) {
        public static PolicyDto from(PricingPolicy p) {
            return new PolicyDto(
                    p.getCode(), p.getName(), p.getProductCode(), p.getChannel(), p.getAudience(),
                    p.getCountryCode(), p.getRailCode(), p.getFeeBearer(), p.getRateCode(),
                    p.getPriority(), p.isActive());
        }
    }
}
