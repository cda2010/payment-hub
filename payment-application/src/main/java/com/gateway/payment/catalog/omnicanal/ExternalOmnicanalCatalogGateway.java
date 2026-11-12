package com.gateway.payment.catalog.omnicanal;

import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.omnicanal.external.ExternalOmnicanalClient;
import com.gateway.payment.omnicanal.external.ExternalPresentationRequest;
import com.gateway.payment.omnicanal.external.ExternalPresentationRequest.ExternalParam;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.provider.ProviderExecutionResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fait passer une opération catalogue par l'omnicanal externe (omnic-exp).
 * Une méthode ({@code GETINFOS}, solde…) part en {@code boundaryByTypeMethode}.
 * Un virement part en {@code omnicanalPresentation}, service 005.
 * Un retrait compte (cash-out agent) part avec le service 009.
 */
@Component
public class ExternalOmnicanalCatalogGateway {

    private static final Pattern SOLDE_TAG = Pattern.compile("(?i)<solde>\\s*([^<]+)\\s*</solde>");
    private static final String ACCOUNT_TO_ACCOUNT = "005";
    private static final String ACCOUNT_WITHDRAWAL = "009";
    private static final String CASH_TO_ACCOUNT = "003";

    private final ExternalOmnicanalClient client;

    public ExternalOmnicanalCatalogGateway(ExternalOmnicanalClient client) {
        this.client = client;
    }

    public boolean enabled() {
        return client != null && client.enabled();
    }

    public ProviderExecutionResult boundary(
            PaymentTransaction tx,
            String operationCode,
            String typeMethode,
            String codeFse,
            Map<String, String> userParams
    ) {
        Map<String, String> params = userParams == null ? new LinkedHashMap<>() : new LinkedHashMap<>(userParams);
        alias(params, "telephone", "destPhone", "phoneNumber", "phone", "msisdn");
        if ("GETINFOS".equalsIgnoreCase(typeMethode)) {
            params = getInfosParams(params);
        } else {
            alias(params, "code", "destAccount", "accountNumber", "sourceAccount", "compteDebiteur");
        }
        if ("CONSULTATIONSOLDE".equalsIgnoreCase(typeMethode)) {
            alias(params, "numeroCompte", "sourceAccount", "compteDebiteur", "accountNumber", "accountId");
            alias(params, "codeFse", "codeFseSource", "CODEFSESOURCE");
            if (StringUtils.hasText(codeFse)) {
                params.putIfAbsent("codeFse", codeFse);
            }
            Map<String, String> solde = new LinkedHashMap<>();
            if (StringUtils.hasText(params.get("numeroCompte"))) {
                solde.put("numeroCompte", params.get("numeroCompte"));
            }
            if (StringUtils.hasText(params.get("codeFse"))) {
                solde.put("codeFse", params.get("codeFse"));
            }
            params = solde;
        }
        Map<String, Object> body = client.boundary(codeFse, typeMethode, ExternalOmnicanalClient.otrParametre(params));
        return toResult(body, operationCode, typeMethode, codeFse, false);
    }

    public ProviderExecutionResult presentation(
            PaymentTransaction tx,
            String sourceFse,
            String destFse,
            Map<String, String> userParams
    ) {
        Map<String, String> params = userParams == null ? Map.of() : userParams;
        String service = serviceCode(tx, params);
        String[] accounts = transferAccounts(service, params);
        String sourceAccount = accounts[0];
        String destAccount = accounts[1];
        String amount = amount(first(params, "amount", "montant", "MONTANTHT"));
        String fees = amount(first(params, "fees", "MONTANTCOMM"));
        if (!StringUtils.hasText(fees)) {
            fees = "0";
        }
        // paymentId, pas le client : la référence sert aussi de clé d'idempotence partenaire.
        String reference = first(params, "paymentId", "transactionReference", "idempotencyKey");
        if (!StringUtils.hasText(reference) && tx != null) {
            reference = tx.getPaymentId();
        }
        if (!StringUtils.hasText(reference)) {
            reference = first(params, "reference", "customerReference");
        }
        String description = first(params, "description", "DESCRIPTION");
        if (!StringUtils.hasText(description)) {
            String kind = ACCOUNT_WITHDRAWAL.equals(service) ? "Retrait " : "Transfert ";
            description = kind + (reference == null ? "" : reference);
        }
        String canal = first(params, "canal", "CANAL");
        if (!StringUtils.hasText(canal)) {
            canal = "MOBILE";
        }

        List<ExternalParam> fields = new ArrayList<>();
        add(fields, "CODEFSESOURCE", sourceFse);
        add(fields, "CODEFSEDEST", destFse);
        add(fields, "codeFseDebit", sourceFse);
        add(fields, "codeFseCredit", destFse);
        add(fields, "compteDebiteur", sourceAccount);
        add(fields, "compteCredit", destAccount);
        add(fields, "CPTECLIENT", sourceAccount);
        add(fields, "CPTEBENEFICIAIRE", destAccount);
        if (ACCOUNT_WITHDRAWAL.equals(service)) {
            add(fields, "CPTEOPERATIONACTEUR", destAccount);
        }
        add(fields, "CPTESRCFRAIS", sourceAccount);
        add(fields, "montant", amount);
        add(fields, "MONTANTHT", amount);
        add(fields, "MONTANTTAXE", "0");
        add(fields, "MONTANTCOMM", fees);
        add(fields, "MONTANTHTCOM", "0");
        add(fields, "MONTANTHTCOMACQ", "0");
        add(fields, "MONTANTCOMACQ", "0");
        add(fields, "MONTANTCOMEMT", "0");
        add(fields, "reference", reference);
        add(fields, "REFERENCE", reference);
        add(fields, "description", description);
        add(fields, "DESCRIPTION", description);
        add(fields, "canal", canal);
        add(fields, "CANAL", canal);
        add(fields, "CAS", "NORMAL");
        add(fields, "otp", first(params, "otp", "codeOtp", "codeOTP", "CODE_OTP", "code_otp"));
        if (ACCOUNT_WITHDRAWAL.equals(service)) {
            add(fields, "CODEAGENCESOURCE", first(params, "CODEAGENCESOURCE", "codeAgenceSource"));
            add(fields, "CODEAGENCEDEST", first(params, "CODEAGENCEDEST", "codeAgenceDest"));
            if (!StringUtils.hasText(first(params, "CODEAGENCESOURCE", "codeAgenceSource"))) {
                add(fields, "CODEAGENCESOURCE", "0");
            }
            if (!StringUtils.hasText(first(params, "CODEAGENCEDEST", "codeAgenceDest"))) {
                add(fields, "CODEAGENCEDEST", "0");
            }
        }

        ExternalPresentationRequest request = new ExternalPresentationRequest(
                canal,
                reference,
                service,
                sourceFse,
                "FSESOURCE",
                "01",
                fields
        );
        Map<String, Object> body = client.presentation(request);
        return toResult(body, "TRANSFERT", "TRANSFERT", sourceFse, true);
    }

    static ProviderExecutionResult toResult(
            Map<String, Object> body,
            String operationCode,
            String typeMethode,
            String codeFse,
            boolean transfer
    ) {
        Map<String, Object> payload = body == null ? new LinkedHashMap<>() : new LinkedHashMap<>(body);
        String nestedCode = nestedText(payload.get("responseEntity"), "codeErr");
        String nestedMsg = nestedText(payload.get("responseEntity"), "msg");
        copyMissing(payload, payload.get("responseEntity"));
        String codeErr = text(payload.get("codeErr"));
        if (!StringUtils.hasText(codeErr)) {
            codeErr = text(payload.get("errCode"));
        }
        String statut = text(payload.get("statut"));
        boolean success = isZero(codeErr) || "SUCCES".equalsIgnoreCase(statut);
        String message = StringUtils.hasText(nestedCode) && !isZero(nestedCode) && StringUtils.hasText(nestedMsg)
                ? nestedMsg
                : firstText(payload, "msg", "text", "message");
        payload.put("omnicanal", true);
        payload.put("externalOmnicanal", true);
        payload.put("typeMethode", typeMethode);
        payload.put("codeFse", codeFse);
        payload.put("operationCode", operationCode);
        if (transfer) {
            payload.put("omnicanalTransfer", true);
        }
        if ("CONSULTATIONSOLDE".equalsIgnoreCase(typeMethode)) {
            Object solde = payload.get("solde");
            if (solde == null) {
                solde = payload.get("balance");
            }
            if (solde == null) {
                solde = soldeFromText(payload.get("text"));
            }
            if (solde != null) {
                payload.put("solde", solde);
                payload.put("balance", solde);
            }
        }
        PaymentStatus gatewayStatus = success ? PaymentStatus.PROCESSING : PaymentStatus.FAILED;
        if ("10".equals(codeErr)) {
            gatewayStatus = PaymentStatus.OTP_REQUIRED;
        } else if ("20".equals(codeErr)) {
            gatewayStatus = PaymentStatus.WAITING_CALLBACK;
        }
        return ProviderExecutionResult.builder()
                .success(success)
                .gatewayStatus(gatewayStatus)
                .providerStatus(StringUtils.hasText(codeErr) ? codeErr : statut)
                .providerMessage(message)
                .providerReference(reference(payload))
                .responseBody(payload)
                .build();
    }

    static String serviceCode(PaymentTransaction tx, Map<String, String> params) {
        String explicit = first(params, "codeService", "codeTypeService");
        if (StringUtils.hasText(explicit)) {
            return explicit;
        }
        String product = first(params, "productCode", "catalogProductCode");
        String step = tx != null && tx.getCurrentStep() != null
                ? tx.getCurrentStep().toUpperCase(Locale.ROOT) : "";
        if (step.contains("CASHOUT") || "CASH_OUT".equalsIgnoreCase(product)) {
            return ACCOUNT_WITHDRAWAL;
        }
        if (step.contains("CASHIN") || "CASH_IN".equalsIgnoreCase(product)) {
            return CASH_TO_ACCOUNT;
        }
        return ACCOUNT_TO_ACCOUNT;
    }

    /**
     * Cash-in : le float agent est débité, le compte client déjà choisi est crédité.
     * Les autres virements gardent source → débit et bénéficiaire → crédit.
     */
    static String[] transferAccounts(String service, Map<String, String> params) {
        String source = first(params, "sourceAccount", "compteDebiteur", "debitAccount", "CPTECLIENT");
        String dest = beneficiaryAccount(params);
        if (CASH_TO_ACCOUNT.equals(service)) {
            String agent = first(params, "agentAccount");
            if (StringUtils.hasText(agent)) {
                source = agent;
            }
        }
        return new String[] {source, dest};
    }

    /** Compte crédité : wallet destinataire, ou compte de l'unité agent sur un retrait. */
    static String beneficiaryAccount(Map<String, String> params) {
        return first(params,
                "destAccount",
                "destinationAccount",
                "agentAccount",
                "compteCredit",
                "beneficiaryAccount",
                "CPTEBENEFICIAIRE");
    }

    private static void alias(Map<String, String> params, String target, String... sources) {
        if (StringUtils.hasText(params.get(target))) {
            return;
        }
        String value = first(params, sources);
        if (StringUtils.hasText(value)) {
            params.put(target, value);
        }
    }

    private static void add(List<ExternalParam> fields, String code, String value) {
        if (StringUtils.hasText(code) && StringUtils.hasText(value)) {
            fields.add(new ExternalParam(code, value, "FSESOURCE"));
        }
    }

    /**
     * GETINFOS attend le mobile local et l'indicatif pays ({@code 226}),
     * pas le numéro de compte.
     */
    private static Map<String, String> getInfosParams(Map<String, String> params) {
        String raw = first(params, "telephone", "destPhone", "phoneNumber", "phone", "msisdn");
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        String indicatif = first(params, "codeTelephone", "codePaysTelephone", "indicatif");
        String local = digits;
        if (digits.startsWith("226") && digits.length() == 11) {
            indicatif = "226";
            local = digits.substring(3);
        } else if (digits.length() == 8 && !StringUtils.hasText(indicatif)) {
            indicatif = "226";
        }
        Map<String, String> infos = new LinkedHashMap<>();
        if (StringUtils.hasText(local)) {
            infos.put("telephone", local);
        }
        if (StringUtils.hasText(indicatif)) {
            infos.put("code", indicatif);
        }
        return infos;
    }

    private static String nestedText(Object nested, String key) {
        if (!(nested instanceof Map<?, ?> map)) {
            return null;
        }
        return text(map.get(key));
    }

    private static void copyMissing(Map<String, Object> payload, Object nested) {
        if (!(nested instanceof Map<?, ?> map)) {
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            payload.putIfAbsent(String.valueOf(entry.getKey()), entry.getValue());
        }
    }

    private static String soldeFromText(Object text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = SOLDE_TAG.matcher(String.valueOf(text));
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private static String amount(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim()).stripTrailingZeros().toPlainString();
        } catch (NumberFormatException ex) {
            return raw.trim();
        }
    }

    @SuppressWarnings("unchecked")
    private static String reference(Map<String, Object> payload) {
        Object operation = payload.get("operation");
        if (operation instanceof Map<?, ?> map) {
            String ref = text(map.get("referenceExterne"));
            if (StringUtils.hasText(ref)) {
                return ref;
            }
            ref = text(map.get("code"));
            if (StringUtils.hasText(ref)) {
                return ref;
            }
        }
        return firstText(payload, "reference", "providerReference");
    }

    private static String first(Map<String, String> params, String... keys) {
        if (params == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            String value = params.get(key);
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static String firstText(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            String value = text(payload.get(key));
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private static String text(Object value) {
        if (value == null) {
            return null;
        }
        String s = String.valueOf(value).trim();
        return s.isEmpty() || "null".equals(s) ? null : s;
    }

    private static boolean isZero(String code) {
        return "0".equals(code);
    }
}
