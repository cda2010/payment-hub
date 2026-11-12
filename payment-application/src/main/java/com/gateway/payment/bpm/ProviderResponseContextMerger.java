package com.gateway.payment.bpm;

import com.gateway.payment.billing.connector.BillResponseSupport;
import com.gateway.payment.billing.connector.dto.BillResponse;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fusionne le body fournisseur dans le contexte paiement (montant lookup, alias, audit).
 * <p>
 * Contrat fixe facture : {@link BillResponse} ({@code bills[]}, {@code reference}, {@code amountDue}, …).
 * Pas de mapping déclaratif IU RESPONSE — la normalisation se fait côté connecteur / adaptateur.
 */
@Component
public class ProviderResponseContextMerger {

    private final ProviderCallOperationRegistry operationRegistry;
    private final BillResponseSupport billResponseSupport;

    public ProviderResponseContextMerger(
            ProviderCallOperationRegistry operationRegistry,
            BillResponseSupport billResponseSupport
    ) {
        this.operationRegistry = operationRegistry;
        this.billResponseSupport = billResponseSupport;
    }

    public MergeResult merge(
            PaymentTransaction tx,
            Map<String, Object> context,
            String operationCode,
            Map<String, Object> rawBody,
            Map<String, Object> flowVars
    ) {
        if (rawBody == null || rawBody.isEmpty() || context == null) {
            return MergeResult.empty();
        }
        ProviderCallOperationSpec spec = operationRegistry.resolve(operationCode);

        Optional<BillResponse> billResponse = billResponseSupport.tryParse(rawBody);
        if (billResponse.isPresent()
                && (spec.onSuccess() == ProviderCallSuccessHandler.BILL_LOOKED_UP
                || spec.onSuccess() == ProviderCallSuccessHandler.BILL_LISTED
                || "LOOKUP_BILL".equalsIgnoreCase(operationCode)
                || "BILL_LIST".equalsIgnoreCase(operationCode)
                || "GETINFOS".equalsIgnoreCase(operationCode)
                || "GET_INFOS".equalsIgnoreCase(operationCode))) {
            boolean mapAmount = spec.mapAmountFromLookup()
                    || spec.onSuccess() == ProviderCallSuccessHandler.BILL_LOOKED_UP;
            billResponseSupport.applyToContext(billResponse.get(), tx, context, mapAmount, flowVars);
            context.put("responseMappedBy", "BILL_RESPONSE_CONTRACT");
            Optional<AuditHint> audit = auditHint(spec, tx, context);
            return new MergeResult(true, mapAmount && tx != null && tx.getAmount() != null, audit);
        }

        Map<String, Object> responseBody = new HashMap<>(rawBody);

        if (spec.remapLookupReference() && responseBody.containsKey("reference")) {
            Object lookupId = responseBody.remove("reference");
            if (lookupId != null) {
                responseBody.put("lookupReference", lookupId);
            }
        }

        context.putAll(responseBody);
        if ("GET_BALANCE".equalsIgnoreCase(operationCode)
                || "CONSULTATIONSOLDE".equalsIgnoreCase(operationCode)
                || "CHECK_BALANCE".equalsIgnoreCase(operationCode)) {
            Object solde = firstPresent(responseBody.get("solde"), responseBody.get("balance"),
                    responseBody.get("availableBalance"), soldeFromText(responseBody.get("text")));
            if (solde != null) {
                context.put("solde", solde);
                context.put("balance", solde);
            }
        }

        // INFOCLIENT : normalise les alias connecteur vers les clés carnet / récap.
        if ("INFOCLIENT".equalsIgnoreCase(operationCode)
                || "INFO_CLIENT".equalsIgnoreCase(operationCode)) {
            putIfAbsent(context, "beneficiaryName", responseBody.get("nom"), responseBody.get("titulaire"),
                    responseBody.get("accountName"), responseBody.get("name"));
            putIfAbsent(context, "label", context.get("beneficiaryName"));
            putIfAbsent(context, "phoneNumber", responseBody.get("telephone"), responseBody.get("msisdn"),
                    responseBody.get("phone"));
            putIfAbsent(context, "accountNumber", responseBody.get("numeroCompte"),
                    responseBody.get("accountNumber"));
        }

        Object url = responseBody.get("redirectUrl");
        if (url == null) {
            url = responseBody.get("url");
        }
        if (url != null) {
            context.put("redirectUrl", url);
        }

        if (spec.onSuccess() == ProviderCallSuccessHandler.BILL_LISTED
                && responseBody.get("count") != null
                && context.get("billCount") == null) {
            context.put("billCount", responseBody.get("count"));
        }

        boolean amountChanged = false;
        if (spec.mapAmountFromLookup()) {
            Object due = responseBody.get("amountDue");
            if (due == null) {
                due = responseBody.get("amount");
            }
            if (due == null) {
                due = responseBody.get("montant");
            }
            if (due != null && tx != null) {
                try {
                    BigDecimal amount = new BigDecimal(String.valueOf(due));
                    tx.setAmount(amount);
                    context.put("amount", amount);
                    if (flowVars != null) {
                        flowVars.put("amount", amount);
                    }
                    amountChanged = true;
                } catch (NumberFormatException ignored) {
                    // ignore malformed amount
                }
            }
        }

        Optional<AuditHint> audit = auditHint(spec, tx, context);
        return new MergeResult(true, amountChanged, audit);
    }

    public MergeResult merge(
            PaymentTransaction tx,
            Map<String, Object> context,
            String operationCode,
            Map<String, Object> rawBody
    ) {
        return merge(tx, context, operationCode, rawBody, null);
    }

    private static Optional<AuditHint> auditHint(
            ProviderCallOperationSpec spec,
            PaymentTransaction tx,
            Map<String, Object> context
    ) {
        return switch (spec.onSuccess()) {
            case BILL_LOOKED_UP -> Optional.of(new AuditHint(
                    "BILL_LOOKED_UP",
                    "Facture trouvée: " + context.getOrDefault(
                            "billLabel",
                            (tx != null ? tx.getAmount() + " " + tx.getCurrency() : "?"))));
            case BILL_LISTED -> Optional.of(new AuditHint(
                    "BILL_LISTED",
                    "Factures listées: " + context.getOrDefault("billCount", "?")));
            default -> Optional.empty();
        };
    }

    public record AuditHint(String eventType, String message) {
    }

    private static final Pattern SOLDE_TAG = Pattern.compile("(?i)<solde>\\s*([^<]+)\\s*</solde>");

    private static Object firstPresent(Object... candidates) {
        if (candidates == null) {
            return null;
        }
        for (Object candidate : candidates) {
            if (candidate != null && !String.valueOf(candidate).isBlank()) {
                return candidate;
            }
        }
        return null;
    }

    private static String soldeFromText(Object text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = SOLDE_TAG.matcher(String.valueOf(text));
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private static void putIfAbsent(Map<String, Object> context, String key, Object... candidates) {
        if (context == null || key == null || context.get(key) != null) {
            return;
        }
        if (candidates == null) {
            return;
        }
        for (Object c : candidates) {
            if (c != null && String.valueOf(c).trim().length() > 0) {
                context.put(key, c);
                return;
            }
        }
    }

    public record MergeResult(boolean contextChanged, boolean amountChanged, Optional<AuditHint> audit) {
        static MergeResult empty() {
            return new MergeResult(false, false, Optional.empty());
        }
    }
}
