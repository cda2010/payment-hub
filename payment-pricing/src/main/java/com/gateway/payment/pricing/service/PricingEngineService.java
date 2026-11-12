package com.gateway.payment.pricing.service;

import com.gateway.payment.pricing.api.dto.PricingDtos.*;
import com.gateway.payment.pricing.domain.*;
import com.gateway.payment.pricing.repository.*;
import com.gateway.payment.pricing.tarif.TarifException;
import com.gateway.payment.pricing.tarif.moteur.DemandeFrais;
import com.gateway.payment.pricing.tarif.moteur.MoteurFrais;
import com.gateway.payment.pricing.tarif.moteur.ResultatFrais;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Devis complet d'une opération : taux via la policy produit / canal / audience,
 * frais via le moteur de tarification (grille publiée).
 * Les plafonds / cumuls réglementaires sont gérés par {@code payment-rules}.
 */
@Service
@Transactional(readOnly = true)
public class PricingEngineService {

    private final PricingPolicyRepository policies;
    private final PricingRateRepository rates;
    private final MoteurFrais moteur;
    private final Clock clock;

    public PricingEngineService(
            PricingPolicyRepository policies,
            PricingRateRepository rates,
            MoteurFrais moteur,
            Clock clock
    ) {
        this.policies = policies;
        this.rates = rates;
        this.moteur = moteur;
        this.clock = clock;
    }

    public QuoteResponse quote(QuoteRequest req) {
        if (req == null || req.amount() == null || req.amount().signum() <= 0) {
            throw new IllegalArgumentException("amount > 0 required");
        }
        BigDecimal amount = req.amount().setScale(2, RoundingMode.HALF_UP);
        String currency = nz(req.currency(), "XOF");

        PricingPolicy policy = resolvePolicy(req);
        List<String> violations = new ArrayList<>();

        // ——— Rate / FX ———
        String rateCode = policy != null ? policy.getRateCode() : null;
        PricingRate rate = resolveRate(rateCode, req.fromCurrency(), req.toCurrency(), currency);
        BigDecimal appliedRate = null;
        BigDecimal converted = amount;
        if (rate != null) {
            appliedRate = rate.getRate();
            String from = nz(req.fromCurrency(), currency);
            String to = nz(req.toCurrency(), currency);
            if (!from.equalsIgnoreCase(to)) {
                if (from.equalsIgnoreCase(rate.getBaseCurrency())
                        && to.equalsIgnoreCase(rate.getQuoteCurrency())) {
                    converted = amount.multiply(appliedRate).setScale(2, RoundingMode.HALF_UP);
                } else if (from.equalsIgnoreCase(rate.getQuoteCurrency())
                        && to.equalsIgnoreCase(rate.getBaseCurrency())
                        && appliedRate.signum() != 0) {
                    converted = amount.divide(appliedRate, 2, RoundingMode.HALF_UP);
                }
            }
        }

        // ——— Fees (assiette résolue par la grille : montant / FX / cumul / override) ———
        PricingFeeBearer bearer = policy != null && policy.getFeeBearer() != null
                ? policy.getFeeBearer()
                : PricingFeeBearer.SENDER;
        ResultatFrais frais = null;
        try {
            frais = moteur.calculer(new DemandeFrais(
                    req.productCode(),
                    req.channel(),
                    currency,
                    amount,
                    LocalDate.now(clock),
                    req.source(),
                    req.destination(),
                    converted,
                    req.feeBaseAmount(),
                    req.aggregates()));
        } catch (TarifException e) {
            if (!"AUCUNE_GRILLE_APPLICABLE".equals(e.getCode())) {
                violations.add("Frais : " + e.getMessage());
            }
        }
        BigDecimal fee = frais != null ? frais.frais() : BigDecimal.ZERO;

        BigDecimal totalDebit;
        BigDecimal amountReceived;
        if (bearer == PricingFeeBearer.RECIPIENT) {
            totalDebit = amount;
            amountReceived = amount.subtract(fee).max(BigDecimal.ZERO);
        } else {
            totalDebit = amount.add(fee);
            amountReceived = amount;
        }

        boolean allowed = violations.isEmpty();
        String snapshotId = "PRICE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new QuoteResponse(
                allowed,
                List.copyOf(violations),
                amount,
                fee,
                converted,
                totalDebit,
                amountReceived,
                appliedRate,
                frais != null ? frais.grilleCode() : null,
                frais != null ? frais.versionGrille() : null,
                frais != null ? frais.formuleCode() : null,
                rate != null ? rate.getCode() : null,
                policy != null ? policy.getCode() : null,
                bearer.name(),
                frais != null ? breakdown(frais) : "Sans frais",
                snapshotId,
                frais != null ? frais.montant() : null,
                frais != null && frais.baseCalcul() != null ? frais.baseCalcul().name() : null,
                frais != null && frais.cumulPeriod() != null ? frais.cumulPeriod().name() : null,
                frais != null && frais.cumulScope() != null ? frais.cumulScope().name() : null
        );
    }

    public List<RateDto> listRates() {
        return rates.findByActiveTrueOrderByCodeAsc().stream().map(RateDto::from).toList();
    }

    public List<PolicyDto> listPolicies() {
        return policies.findByActiveTrueOrderByPriorityDesc().stream().map(PolicyDto::from).toList();
    }

    private PricingPolicy resolvePolicy(QuoteRequest req) {
        List<PricingPolicy> candidates = req.productCode() != null && !req.productCode().isBlank()
                ? policies.findByProductCodeAndActiveTrueOrderByPriorityDesc(req.productCode().trim())
                : policies.findByActiveTrueOrderByPriorityDesc();

        return candidates.stream()
                .filter(p -> matchDim(p.getChannel(), req.channel()))
                .filter(p -> matchDim(p.getAudience(), req.audience()))
                .filter(p -> matchDim(p.getCountryCode(), req.countryCode()))
                .filter(p -> matchDim(p.getRailCode(), req.railCode()))
                .max(Comparator
                        .comparingInt(PricingPolicy::getPriority)
                        .thenComparingInt(this::specificity))
                .orElse(null);
    }

    private int specificity(PricingPolicy p) {
        int s = 0;
        if (notBlank(p.getProductCode())) s += 8;
        if (notBlank(p.getChannel())) s += 4;
        if (notBlank(p.getAudience())) s += 4;
        if (notBlank(p.getCountryCode())) s += 2;
        if (notBlank(p.getRailCode())) s += 2;
        return s;
    }

    private boolean matchDim(String policyVal, String reqVal) {
        if (!notBlank(policyVal)) return true; // wildcard
        if (!notBlank(reqVal)) return false;
        return policyVal.equalsIgnoreCase(reqVal.trim());
    }

    private PricingRate resolveRate(String rateCode, String from, String to, String fallbackCcy) {
        Instant now = Instant.now();
        if (notBlank(rateCode)) {
            return rates.findByCodeAndActiveTrue(rateCode)
                    .filter(r -> validNow(r, now))
                    .orElse(null);
        }
        String base = nz(from, fallbackCcy);
        String quote = nz(to, fallbackCcy);
        if (base.equalsIgnoreCase(quote)) return null;
        return rates.findByBaseCurrencyAndQuoteCurrencyAndActiveTrue(base.toUpperCase(), quote.toUpperCase())
                .stream()
                .filter(r -> validNow(r, now))
                .findFirst()
                .orElse(null);
    }

    private boolean validNow(PricingRate r, Instant now) {
        if (r.getValidFrom() != null && now.isBefore(r.getValidFrom())) return false;
        if (r.getValidTo() != null && now.isAfter(r.getValidTo())) return false;
        return true;
    }

    /** Ex. « TRF_STD v1, tranche 2 : (20000 - 8000) * 0.015 + 100 = 280 ». */
    private static String breakdown(ResultatFrais f) {
        String borne = f.ajustement() == null ? ""
                : " → " + f.ajustement().toLowerCase() + " " + texte(f.fraisAvantArrondi());
        return f.grilleCode() + " v" + f.versionGrille() + ", tranche " + f.tranche().ordre() + " : "
                + f.details() + borne + " = " + texte(f.frais());
    }

    private static String texte(BigDecimal v) {
        return v.signum() == 0 ? "0" : v.stripTrailingZeros().toPlainString();
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String nz(String s, String d) {
        return notBlank(s) ? s.trim() : d;
    }
}
