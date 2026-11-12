package com.gateway.payment.catalog.capability;

import com.gateway.payment.bc.IdentityBcClient;
import com.gateway.payment.bc.RulesBcClient;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.inbox.PartyActionProducer;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Évalue {@code payment-rules} (plafonds / cumuls) et réserve si ALLOW.
 * KYC / customerStatus : Identity BC si disponible, sinon défauts.
 */
@Component
public class EvaluateRulesCapabilityHandler implements CapabilityHandler {

    private final SoftRuleCapabilitySupport softRules;
    private final RulesBcClient rules;
    private final IdentityBcClient identity;

    public EvaluateRulesCapabilityHandler(
            SoftRuleCapabilitySupport softRules,
            RulesBcClient rules,
            IdentityBcClient identity
    ) {
        this.softRules = softRules;
        this.rules = rules;
        this.identity = identity;
    }

    @Override
    public String code() {
        return CapabilityCodes.EVALUATE_RULES;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        SoftRuleCapabilitySupport.SoftBundle soft = softRules.apply(context.step(), context.context());
        List<CapabilityResult.CapabilityEvent> events = new ArrayList<>(soft.events());
        Map<String, Object> vars = soft.vars();

        if (!rules.enabled()) {
            events.add(new CapabilityResult.CapabilityEvent("RULES_SKIPPED", "payment-rules désactivé"));
            return CapabilityResult.softOnly(soft.matched(), events, vars);
        }

        Map<String, Object> ctx = context.context();
        PaymentTransaction tx = context.transaction();
        String paymentId = context.paymentId() != null
                ? context.paymentId()
                : (tx != null ? tx.getPaymentId() : null);

        BigDecimal amount = toDecimal(ctx.get("amount"));
        if (amount == null && tx != null) {
            amount = tx.getAmount();
        }
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        String productCode = firstString(ctx.get("productCode"), ctx.get("catalogProductCode"));
        String groupCode = firstString(
                context.bindingParams() != null ? context.bindingParams().get("groupCode") : null,
                ctx.get("ruleGroupCode"),
                ctx.get("groupCode"),
                productCode != null ? productCode + "_STANDARD" : null,
                "CASH_OUT_STANDARD");
        // Prefer productCode over ServiceType (TRANSFERT) so rule conditions match CASH_IN / CASH_OUT.
        String transactionType = firstString(
                context.bindingParams() != null ? context.bindingParams().get("transactionType") : null,
                ctx.get("transactionType"),
                productCode,
                tx != null && tx.getServiceType() != null ? tx.getServiceType().name() : null,
                "PAYMENT");

        String actorRole = firstString(
                context.binding() != null ? context.binding().actorRole() : null,
                ctx.get("catalogActorRole"));
        if (actorRole == null && "BENEFICIARY".equalsIgnoreCase(firstString(
                context.bindingParams() != null ? context.bindingParams().get("subject") : null))) {
            actorRole = "DESTINATION";
        }
        String sourceCustomer = firstString(ctx.get("sourceCustomerId"), ctx.get("payerCustomerId"));
        String destCustomer = firstString(
                ctx.get("destCustomerId"), ctx.get("beneficiaryCustomerId"), ctx.get("destinationCustomerId"));
        String sourceAccount = firstString(ctx.get("sourceAccount"));
        String destAccount = firstString(ctx.get("destAccount"), ctx.get("destinationAccount"));
        boolean receiver = RulesParty.separateReceiver(
                actorRole, sourceCustomer, destCustomer, sourceAccount, destAccount);

        Map<String, Object> ruleCtx = new HashMap<>();
        String customerId;
        String walletId;
        if (receiver) {
            copyIfPresent(ctx, ruleCtx, "country", "channel", "canal", "subjectRole", "participantKind", "role");
            if (ctx.get("channel") == null && ctx.get("canal") != null) {
                ruleCtx.put("channel", ctx.get("canal"));
            }
            BigDecimal holding = firstDecimal(
                    ctx.get("beneficiaryHolding"), ctx.get("holdingBalance"),
                    ctx.get("beneficiaryBalance"), ctx.get("destBalance"), ctx.get("destinationBalance"));
            if (holding != null) {
                ruleCtx.put("walletBalance", holding);
            } else {
                events.add(new CapabilityResult.CapabilityEvent(
                        "BENEFICIARY_HOLDING_UNKNOWN",
                        "Détention consolidée du bénéficiaire absente : le plafond porte sur le montant reçu"));
            }
            String payer = firstString(sourceCustomer, ctx.get("customerId"), tx != null ? tx.getCustomerReference() : null);
            if (same(payer, destCustomer) || same(sourceAccount, destAccount)) {
                ruleCtx.put("sameCustomer", "true");
            }
            if (payer != null) {
                ruleCtx.put("sourceCustomerId", payer);
            }
            customerId = destCustomer != null ? destCustomer : destAccount;
            walletId = destAccount;
            ensureRuleContextDefaults(ruleCtx, ctx, false);
        } else if ("SOURCE".equalsIgnoreCase(actorRole)) {
            copyIfPresent(ctx, ruleCtx, "kycLevel", "niveauKyc", "customerStatus", "country", "channel", "canal",
                    "subjectRole", "participantKind", "role");
            if (ctx.get("channel") == null && ctx.get("canal") != null) {
                ruleCtx.put("channel", ctx.get("canal"));
            }
            BigDecimal sourceBalance = firstDecimal(ctx.get("sourceBalance"), ctx.get("walletBalance"), ctx.get("balance"));
            if (sourceBalance != null) {
                ruleCtx.put("walletBalance", sourceBalance);
            }
            enrichFromIdentity(ruleCtx, ctx);
            ensureRuleContextDefaults(ruleCtx, ctx, true);
            customerId = firstString(sourceCustomer, ctx.get("customerId"), tx != null ? tx.getCustomerReference() : null);
            walletId = firstString(sourceAccount, ctx.get("walletId"), ctx.get("accountId"));
        } else {
            copyIfPresent(ctx, ruleCtx, "walletBalance", "balance", "kycLevel", "niveauKyc",
                    "customerStatus", "country", "channel", "canal",
                    "subjectRole", "participantKind", "role");
            if (ctx.get("channel") == null && ctx.get("canal") != null) {
                ruleCtx.put("channel", ctx.get("canal"));
            }
            if (same(sourceCustomer, destCustomer) || same(sourceAccount, destAccount)) {
                ruleCtx.put("sameCustomer", "true");
            }
            if (sourceCustomer != null) {
                ruleCtx.put("sourceCustomerId", sourceCustomer);
            }
            enrichFromIdentity(ruleCtx, ctx);
            ensureRuleContextDefaults(ruleCtx, ctx, true);
            customerId = firstString(ctx.get("customerId"), tx != null ? tx.getCustomerReference() : null);
            walletId = firstString(ctx.get("walletId"), ctx.get("accountId"));
        }

        RulesBcClient.EvaluationResult result = rules.evaluate(new RulesBcClient.EvaluateCommand(
                paymentId,
                transactionType,
                productCode,
                groupCode,
                amount,
                firstString(ctx.get("currency"), tx != null ? tx.getCurrency() : null, "XOF"),
                customerId,
                walletId,
                firstString(ctx.get("agentId")),
                firstString(ctx.get("merchantId"), tx != null ? tx.getMerchantReference() : null),
                ruleCtx
        ));

        ctx.put("rulesDecision", result.decision());
        vars.put("rulesDecision", result.decision());
        ctx.put("rulesAuthorized", result.authorized());
        vars.put("rulesAuthorized", result.authorized());
        if (result.reservationId() != null) {
            ctx.put("ruleReservationId", result.reservationId().toString());
            vars.put("ruleReservationId", result.reservationId().toString());
        }
        if (result.evaluatedRules() != null) {
            ctx.put("rulesEvaluated", result.evaluatedRules());
        }

        String decision = result.decision() != null ? result.decision().toUpperCase() : "REJECT";
        events.add(new CapabilityResult.CapabilityEvent(
                "RULES_" + decision,
                result.authorized() ? "Règles OK" : String.join("; ", result.violations())));

        if ("REJECT".equals(decision) || (!result.authorized() && !"REVIEW".equals(decision))) {
            throw new BusinessException(
                    "RULES_REJECTED",
                    result.violations().isEmpty()
                            ? "Transaction refusée par les règles métier"
                            : String.join("; ", result.violations()),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if ("REVIEW".equals(decision)) {
            ctx.put("rulesReviewRequired", true);
            vars.put("rulesReviewRequired", true);
        }

        return CapabilityResult.softOnly(soft.matched(), events, vars);
    }

    private static void copyIfPresent(Map<String, Object> src, Map<String, Object> dest, String... keys) {
        for (String k : keys) {
            if (src.get(k) != null) {
                dest.put(k, src.get(k));
            }
        }
    }

    private void enrichFromIdentity(Map<String, Object> ruleCtx, Map<String, Object> paymentCtx) {
        if (!identity.enabled()) {
            return;
        }
        UUID partyId = PartyActionProducer.partyIdFromContext(paymentCtx);
        if (partyId == null) {
            return;
        }
        identity.getParty(partyId).ifPresent(party -> {
            if (blank(ruleCtx.get("customerStatus")) && party.customerStatus() != null) {
                ruleCtx.put("customerStatus", party.customerStatus());
                paymentCtx.putIfAbsent("customerStatus", party.customerStatus());
            }
            if (blank(ruleCtx.get("kycLevel")) && blank(ruleCtx.get("niveauKyc")) && party.kycLevel() != null) {
                ruleCtx.put("kycLevel", party.kycLevel());
                paymentCtx.putIfAbsent("kycLevel", party.kycLevel());
            }
        });
    }

    private static void ensureRuleContextDefaults(
            Map<String, Object> ruleCtx,
            Map<String, Object> paymentCtx,
            boolean fillUnknownBalance
    ) {
        if (blank(ruleCtx.get("customerStatus"))) {
            ruleCtx.put("customerStatus", "ACTIVE");
            paymentCtx.putIfAbsent("customerStatus", "ACTIVE");
        }
        if (blank(ruleCtx.get("kycLevel")) && blank(ruleCtx.get("niveauKyc"))) {
            ruleCtx.put("kycLevel", "STANDARD");
            paymentCtx.putIfAbsent("kycLevel", "STANDARD");
        }
        if (fillUnknownBalance && blank(ruleCtx.get("walletBalance")) && blank(ruleCtx.get("balance"))) {
            // Solde fictif pour PROJECTED_BALANCE / CHECK_WALLET_BALANCE tant que le ledger n'alimente pas.
            BigDecimal demoBalance = new BigDecimal("1000000");
            ruleCtx.put("walletBalance", demoBalance);
            paymentCtx.putIfAbsent("walletBalance", demoBalance);
        }
    }

    private static boolean same(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private static BigDecimal firstDecimal(Object... values) {
        for (Object value : values) {
            BigDecimal decimal = toDecimal(value);
            if (decimal != null) {
                return decimal;
            }
        }
        return null;
    }

    private static boolean blank(Object v) {
        return v == null || String.valueOf(v).isBlank();
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
