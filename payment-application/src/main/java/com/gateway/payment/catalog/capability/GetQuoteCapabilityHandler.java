package com.gateway.payment.catalog.capability;

import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Capability {@code GET_QUOTE} — cotation remittance (FX, frais, TTL).
 * <p>
 * Sources autorisées uniquement : contexte runtime ({@code fxRate}, {@code fees})
 * ou {@code configuration.remittanceQuote} (catalogue). Aucun taux / frais stub en code.
 */
@Component
public class GetQuoteCapabilityHandler implements CapabilityHandler {

    private final SoftRuleCapabilitySupport softRules;

    public GetQuoteCapabilityHandler(SoftRuleCapabilitySupport softRules) {
        this.softRules = softRules;
    }

    @Override
    public String code() {
        return CapabilityCodes.GET_QUOTE;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        SoftRuleCapabilitySupport.SoftBundle soft = softRules.apply(context.step(), context.context());
        Map<String, Object> vars = soft.vars();
        Map<String, Object> ctx = context.context();

        BigDecimal amount = toDecimal(first(ctx, "amount", "sendAmount", "debitAmount"));
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            amount = context.transaction() != null ? context.transaction().getAmount() : null;
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            amount = BigDecimal.ZERO;
        }

        String sendCurrency = firstNonBlank(
                str(ctx.get("currency")),
                str(ctx.get("sendCurrency")),
                context.transaction() != null ? context.transaction().getCurrency() : null,
                "XOF");
        String receiveCurrency = firstNonBlank(
                str(ctx.get("receiveCurrency")),
                str(ctx.get("destinationCurrency")),
                sendCurrency);

        Map<String, Object> quoteCfg = readMap(configuration, "remittanceQuote");
        BigDecimal fxRate = resolveFxRate(sendCurrency, receiveCurrency, quoteCfg, ctx);
        BigDecimal fees = resolveFees(amount, quoteCfg, ctx);
        BigDecimal receiveAmount = amount.multiply(fxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDebit = amount.add(fees).setScale(2, RoundingMode.HALF_UP);

        int ttlMinutes = toInt(quoteCfg.get("ttlMinutes"), 15);
        Instant expiresAt = Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES);
        String quoteId = "QTE-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);

        put(ctx, vars, "quoteId", quoteId);
        put(ctx, vars, "quoteStatus", "QUOTED");
        put(ctx, vars, "fxRate", fxRate);
        put(ctx, vars, "fees", fees);
        put(ctx, vars, "receiveAmount", receiveAmount);
        put(ctx, vars, "receiveCurrency", receiveCurrency);
        put(ctx, vars, "sendCurrency", sendCurrency);
        put(ctx, vars, "sendAmount", amount);
        put(ctx, vars, "totalDebit", totalDebit);
        put(ctx, vars, "quoteExpiresAt", expiresAt.toString());
        put(ctx, vars, "amount", amount);
        put(ctx, vars, "currency", sendCurrency);

        PaymentTransaction tx = context.transaction();
        if (tx != null) {
            tx.setAmount(amount);
            tx.setCurrency(sendCurrency);
            tx.setCurrentStep("GET_QUOTE");
            tx.applyStatus(PaymentStatus.PROCESSING);
        }

        List<CapabilityResult.CapabilityEvent> events = new ArrayList<>(soft.events());
        events.add(new CapabilityResult.CapabilityEvent(
                "QUOTE",
                "Quote " + quoteId + " — " + receiveAmount + " " + receiveCurrency
                        + " (fx=" + fxRate + ", fees=" + fees + ")"));

        return CapabilityResult.of(
                soft.matched(), events, vars, CapabilityResult.TerminalEffect.PROCESSING, null);
    }

    private static BigDecimal resolveFxRate(
            String send, String receive, Map<String, Object> quoteCfg, Map<String, Object> ctx) {
        BigDecimal fromCtx = toDecimal(ctx.get("fxRate"));
        if (fromCtx != null && fromCtx.compareTo(BigDecimal.ZERO) > 0) {
            return fromCtx;
        }
        if (send.equalsIgnoreCase(receive)) {
            return BigDecimal.ONE;
        }
        Object rates = quoteCfg.get("fxRates");
        if (rates instanceof Map<?, ?> map) {
            Object pair = map.get(send + ">" + receive);
            if (pair == null) {
                pair = map.get(send + "_" + receive);
            }
            BigDecimal r = toDecimal(pair);
            if (r != null && r.compareTo(BigDecimal.ZERO) > 0) {
                return r;
            }
        }
        throw new BusinessException(
                "FX_RATE_REQUIRED",
                "Taux FX manquant pour " + send + ">" + receive
                        + " — fournir context.fxRate ou configuration.remittanceQuote.fxRates");
    }

    private static BigDecimal resolveFees(BigDecimal amount, Map<String, Object> quoteCfg, Map<String, Object> ctx) {
        BigDecimal fromCtx = toDecimal(ctx.get("fees"));
        if (fromCtx != null) {
            return fromCtx;
        }
        BigDecimal flat = toDecimal(quoteCfg.get("defaultFee"));
        if (flat == null) {
            flat = toDecimal(ctx.get("defaultFee"));
        }
        if (flat == null) {
            throw new BusinessException(
                    "QUOTE_FEE_REQUIRED",
                    "Frais de cotation manquants — fournir context.fees ou configuration.remittanceQuote.defaultFee");
        }
        BigDecimal pct = toDecimal(quoteCfg.get("feePercent"));
        if (pct != null && pct.compareTo(BigDecimal.ZERO) > 0) {
            return flat.add(amount.multiply(pct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        }
        return flat.setScale(2, RoundingMode.HALF_UP);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readMap(Map<String, Object> configuration, String key) {
        if (configuration == null) {
            return Map.of();
        }
        Object raw = configuration.get(key);
        if (raw instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        return Map.of();
    }

    private static void put(Map<String, Object> ctx, Map<String, Object> vars, String key, Object value) {
        ctx.put(key, value);
        vars.put(key, value);
    }

    private static Object first(Map<String, Object> ctx, String... keys) {
        for (String k : keys) {
            Object v = ctx.get(k);
            if (v != null) {
                return v;
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
            return new BigDecimal(String.valueOf(v).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static int toInt(Object v, int defaultValue) {
        if (v instanceof Number n) {
            return n.intValue();
        }
        if (v != null) {
            try {
                return Integer.parseInt(String.valueOf(v).trim());
            } catch (Exception ignored) {
                // fall through
            }
        }
        return defaultValue;
    }

    private static String str(Object v) {
        return v != null ? String.valueOf(v).trim() : null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v.trim();
            }
        }
        return null;
    }
}
