package com.gateway.payment.catalog.capability;

import com.gateway.payment.bc.PricingBcClient;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Soft-rules d'abord (DEFAULT_FEE…), sinon appel live {@code payment-pricing/quote}.
 * Assiette : déclarée sur la grille ({@code baseCalcul}) ; {@code feeBaseAmount} = override explicite.
 * Cumuls : {@code feeAggregates} / {@code aggregates} dans le contexte.
 */
@Component
public class CalculateFeesCapabilityHandler implements CapabilityHandler {

    private static final Logger log = LoggerFactory.getLogger(CalculateFeesCapabilityHandler.class);

    private final SoftRuleCapabilitySupport softRules;
    private final PricingBcClient pricing;

    public CalculateFeesCapabilityHandler(SoftRuleCapabilitySupport softRules, PricingBcClient pricing) {
        this.softRules = softRules;
        this.pricing = pricing;
    }

    @Override
    public String code() {
        return CapabilityCodes.CALCULATE_FEES;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        SoftRuleCapabilitySupport.SoftBundle soft = softRules.apply(context.step(), context.context());
        List<CapabilityResult.CapabilityEvent> events = new ArrayList<>(soft.events());
        Map<String, Object> vars = soft.vars();

        boolean feesAlreadySet = context.context().get("fees") != null || vars.containsKey("fees");
        if (!feesAlreadySet && pricing.enabled()) {
            try {
                applyPricingQuote(context, vars, events);
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Pricing live indisponible, fallback fees=0: {}", e.getMessage());
                context.context().put("fees", 0);
                vars.put("fees", 0);
                events.add(new CapabilityResult.CapabilityEvent(
                        "PRICING_FALLBACK", "Pricing indisponible — frais à 0"));
            }
        } else if (!feesAlreadySet) {
            context.context().put("fees", 0);
            vars.put("fees", 0);
        }
        return CapabilityResult.softOnly(soft.matched(), events, vars);
    }

    private void applyPricingQuote(
            CapabilityExecutionContext context,
            Map<String, Object> vars,
            List<CapabilityResult.CapabilityEvent> events
    ) {
        Map<String, Object> ctx = context.context();
        PaymentTransaction tx = context.transaction();
        BigDecimal amount = firstDecimal(ctx.get("amount"), tx != null ? tx.getAmount() : null);
        if (amount == null || amount.signum() <= 0) {
            context.context().put("fees", 0);
            vars.put("fees", 0);
            return;
        }

        // Override uniquement si le parcours / binding le pose explicitement (CUSTOM ou forçage).
        // Sinon la grille résout l'assiette (TRANSACTION_AMOUNT / CONVERTED_AMOUNT / CUMUL_*).
        BigDecimal feeBaseOverride = firstDecimal(
                ctx.get("feeBaseAmount"),
                context.bindingParams() != null ? context.bindingParams().get("feeBaseAmount") : null);

        String productCode = firstString(
                ctx.get("productCode"),
                ctx.get("catalogProductCode"),
                tx != null && tx.getServiceType() != null ? tx.getServiceType().name() : null,
                "GENERIC");
        String channel = firstString(ctx.get("channel"), ctx.get("canal"), "WEB");
        String currency = firstString(
                ctx.get("currency"),
                tx != null ? tx.getCurrency() : null,
                "XOF");

        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> aggregates = aggregatesFrom(ctx.get("feeAggregates"), ctx.get("aggregates"));

        PricingBcClient.QuoteResult quote = pricing.quote(new PricingBcClient.QuoteCommand(
                productCode,
                channel,
                firstString(ctx.get("audience"), "RETAIL"),
                amount,
                currency,
                firstString(ctx.get("fromCurrency"), currency),
                firstString(ctx.get("toCurrency"), currency),
                firstString(ctx.get("countryCode"), ctx.get("country")),
                firstString(ctx.get("railCode"), ctx.get("rail")),
                feeBaseOverride,
                aggregates
        ));

        if (!quote.allowed()) {
            throw new BusinessException(
                    "PRICING_REJECTED",
                    String.join("; ", quote.violations()),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        BigDecimal fees = quote.feeAmount() != null ? quote.feeAmount() : BigDecimal.ZERO;
        ctx.put("fees", fees);
        vars.put("fees", fees);
        if (quote.totalDebit() != null) {
            ctx.put("totalDebit", quote.totalDebit());
            vars.put("totalDebit", quote.totalDebit());
        }
        if (quote.amountReceived() != null) {
            ctx.put("amountReceived", quote.amountReceived());
            vars.put("amountReceived", quote.amountReceived());
        }
        putIfPresent(ctx, vars, "feeGridCode", quote.feeGridCode());
        putIfPresent(ctx, vars, "feeFormulaCode", quote.formulaCode());
        putIfPresent(ctx, vars, "feeBearer", quote.feeBearer());
        putIfPresent(ctx, vars, "feeBreakdown", quote.feeBreakdown());
        putIfPresent(ctx, vars, "pricingSnapshotId", quote.snapshotId());
        putIfPresent(ctx, vars, "feeBaseAmount", quote.feeBaseAmount());
        putIfPresent(ctx, vars, "feeBaseCalcul", quote.baseCalcul());
        events.add(new CapabilityResult.CapabilityEvent(
                "PRICING_QUOTE",
                "Frais=" + fees
                        + (quote.feeGridCode() != null ? " (" + quote.feeGridCode() + ")" : "")
                        + (quote.baseCalcul() != null ? " base=" + quote.baseCalcul() : "")));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, BigDecimal> aggregatesFrom(Object... candidates) {
        for (Object raw : candidates) {
            if (raw instanceof Map<?, ?> map && !map.isEmpty()) {
                Map<String, BigDecimal> out = new java.util.LinkedHashMap<>();
                for (Map.Entry<?, ?> e : map.entrySet()) {
                    if (e.getKey() == null || e.getValue() == null) {
                        continue;
                    }
                    BigDecimal v = firstDecimal(e.getValue());
                    if (v != null) {
                        out.put(String.valueOf(e.getKey()), v);
                    }
                }
                if (!out.isEmpty()) {
                    return out;
                }
            }
        }
        return null;
    }

    private static void putIfPresent(Map<String, Object> ctx, Map<String, Object> vars, String key, Object value) {
        if (value != null) {
            ctx.put(key, value);
            vars.put(key, value);
        }
    }

    private static BigDecimal firstDecimal(Object... values) {
        for (Object v : values) {
            BigDecimal d = toDecimal(v);
            if (d != null) {
                return d;
            }
        }
        return null;
    }

    private static BigDecimal toDecimal(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        if (v instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            String s = String.valueOf(v).trim();
            if (s.isEmpty()) {
                return null;
            }
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String firstString(Object... values) {
        for (Object v : values) {
            if (v != null) {
                String s = String.valueOf(v).trim();
                if (!s.isEmpty()) {
                    return s;
                }
            }
        }
        return null;
    }
}
