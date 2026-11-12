package com.gateway.payment.catalog.capability;

import com.gateway.payment.bc.AliasBcClient;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Capability {@code EXECUTE_CASHOUT} — débit source sur parcours {@code withdrawalChannel=AGENT}.
 * <p>
 * Le code retrait (voucher) est généré sur la branche GAB ({@code GENERATE_VOUCHER}), pas ici.
 * <p>
 * Personnalisations vs transfert générique :
 * <ul>
 *   <li>préconditions cash-out (montant, source, réseau agent, compte destination agent)</li>
 *   <li>compte DEST = alias UNITE ({@code destinationAccount} / {@code externalRef})</li>
 *   <li>{@code terminalOnConfirm=false} — suite → WAIT_AGENT_PAYOUT</li>
 *   <li>événement {@code CASHOUT_DEBITED}</li>
 * </ul>
 */
@Component
public class ExecuteCashoutCapabilityHandler implements CapabilityHandler {

    private static final String OPERATION = "TRANSFERT";

    private final ProviderCapabilitySupport providerSupport;
    private final AliasBcClient aliasBcClient;

    public ExecuteCashoutCapabilityHandler(
            ProviderCapabilitySupport providerSupport,
            AliasBcClient aliasBcClient
    ) {
        this.providerSupport = providerSupport;
        this.aliasBcClient = aliasBcClient;
    }

    @Override
    public String code() {
        return CapabilityCodes.EXECUTE_CASHOUT;
    }

    @Override
    public String defaultOperation() {
        return OPERATION;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        Map<String, Object> ctx = context.context();
        PaymentTransaction tx = context.transaction();

        ensureDestinationAccount(ctx);
        validateCashoutPreconditions(ctx, tx);
        prepareCashoutContext(ctx);

        CapabilityResult provider = providerSupport.invoke(context, OPERATION);

        List<CapabilityResult.CapabilityEvent> events = new ArrayList<>(provider.events());
        events.add(new CapabilityResult.CapabilityEvent(
                "CASHOUT_DEBITED",
                "Débit source cash-out agent OK — DEST=" + firstString(ctx, "destinationAccount")
                        + " — attente remise agent"));

        Map<String, Object> vars = new LinkedHashMap<>(provider.mutableVars());
        vars.put("cashOutDebited", true);
        vars.put("terminalOnConfirm", false);
        vars.put("cashOutPhase", "AWAITING_AGENT_PAYOUT");
        if (ctx.get("destinationAccount") != null) {
            vars.put("destinationAccount", ctx.get("destinationAccount"));
        }

        ctx.put("cashOutDebited", true);
        ctx.put("cashOutPhase", "AWAITING_AGENT_PAYOUT");
        context.setVar("cashOutDebited", true);

        CapabilityResult.TerminalEffect terminal = provider.terminal() == CapabilityResult.TerminalEffect.SUCCESS
                ? CapabilityResult.TerminalEffect.PROCESSING
                : provider.terminal();

        return CapabilityResult.provider(events, vars, terminal, "Cash-out agent débité — suite parcours");
    }

    /**
     * Si {@code destinationAccount} absent mais {@code agentUniteId} présent → alias UNITE.
     */
    private void ensureDestinationAccount(Map<String, Object> ctx) {
        if (StringUtils.hasText(firstString(ctx, "destinationAccount", "agentAccount"))) {
            if (!StringUtils.hasText(firstString(ctx, "destinationAccount"))
                    && StringUtils.hasText(firstString(ctx, "agentAccount"))) {
                ctx.put("destinationAccount", firstString(ctx, "agentAccount"));
            }
            return;
        }
        String uniteIdRaw = firstString(ctx, "agentUniteId");
        if (!StringUtils.hasText(uniteIdRaw) || !aliasBcClient.enabled()) {
            return;
        }
        UUID uniteId;
        try {
            uniteId = UUID.fromString(uniteIdRaw);
        } catch (IllegalArgumentException e) {
            return;
        }
        String rail = firstString(ctx, "agentNetwork", "destinationRail");
        try {
            aliasBcClient.resolveUniteAccount(uniteId, "CORIS_DEMO", rail).ifPresent(a -> {
                ctx.put("destinationAccount", a.externalRef());
                ctx.put("agentAccount", a.externalRef());
                ctx.put("agentAliasCode", a.aliasCode());
                ctx.put("agentAccountType", a.aliasType());
            });
        } catch (IllegalStateException e) {
            throw new BusinessException(
                    "ALIAS_UNAVAILABLE",
                    e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private static void validateCashoutPreconditions(Map<String, Object> ctx, PaymentTransaction tx) {
        BigDecimal amount = toAmount(first(ctx, "amount", "debitAmount", "sendAmount"));
        if (amount == null && tx != null) {
            amount = tx.getAmount();
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "CASHOUT_AMOUNT_REQUIRED",
                    "Montant cash-out manquant ou invalide avant débit.",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        String source = firstString(ctx,
                "sourceAccount", "accountAlias", "walletId", "accountId", "debitAccount");
        if (!StringUtils.hasText(source)) {
            throw new BusinessException(
                    "CASHOUT_SOURCE_REQUIRED",
                    "Compte source (sourceAccount) requis avant débit cash-out.",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        String agentNetwork = firstString(ctx, "agentNetwork", "destinationRail", "providerRail");
        if (!StringUtils.hasText(agentNetwork)) {
            throw new BusinessException(
                    "CASHOUT_AGENT_NETWORK_REQUIRED",
                    "Réseau agent (agentNetwork) requis avant débit cash-out.",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        String dest = firstString(ctx, "destinationAccount", "agentAccount");
        if (!StringUtils.hasText(dest)) {
            throw new BusinessException(
                    "CASHOUT_DEST_REQUIRED",
                    "Compte destination agent introuvable (alias UNITE pour l'unité sélectionnée).",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        ctx.putIfAbsent("destinationAccount", dest);
    }

    private static void prepareCashoutContext(Map<String, Object> ctx) {
        ctx.put("terminalOnConfirm", false);
        ctx.putIfAbsent("productCode", "CASH_OUT");
        ctx.putIfAbsent("catalogProductCode", "CASH_OUT");
        ctx.putIfAbsent("codeService", "009");
        String dest = firstString(ctx, "destinationAccount", "agentAccount");
        if (StringUtils.hasText(dest)) {
            ctx.putIfAbsent("destAccount", dest);
        }
        ctx.put("cashOutPhase", "DEBIT_SOURCE");
    }

    private static BigDecimal toAmount(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            String s = String.valueOf(value).trim().replace(',', '.');
            return s.isEmpty() ? null : new BigDecimal(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Object first(Map<String, Object> ctx, String... keys) {
        if (ctx == null) {
            return null;
        }
        for (String key : keys) {
            Object v = ctx.get(key);
            if (v != null && StringUtils.hasText(String.valueOf(v))) {
                return v;
            }
        }
        return null;
    }

    private static String firstString(Map<String, Object> ctx, String... keys) {
        Object v = first(ctx, keys);
        return v != null ? String.valueOf(v).trim() : null;
    }
}
