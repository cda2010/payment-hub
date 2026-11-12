package com.gateway.payment.bpm;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registre des opérations fournisseur : rail, effets post-succès, mapping activité BPMN.
 * <p>
 * Source unique pour ce qui était hardcodé dans {@link GatewayProviderCallDelegate}.
 */
@Component
public class ProviderCallOperationRegistry {

    private final Map<String, ProviderCallOperationSpec> byOperation = new ConcurrentHashMap<>();

    public ProviderCallOperationRegistry() {
        register(new ProviderCallOperationSpec(
                "LOOKUP_BILL",
                ProviderInvokeRail.BILLER,
                ProviderCallSuccessHandler.BILL_LOOKED_UP,
                true,
                true,
                false,
                null));
        register(new ProviderCallOperationSpec(
                "BILL_LIST",
                ProviderInvokeRail.BILLER,
                ProviderCallSuccessHandler.BILL_LISTED,
                false,
                false,
                false,
                null));
        // Aligné LOOKUP_BILL : montant + audit (legacy PaymentEngine GETINFOS/GET_INFOS)
        register(new ProviderCallOperationSpec(
                "GETINFOS",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.BILL_LOOKED_UP,
                true,
                false,
                false,
                null));
        register(new ProviderCallOperationSpec(
                "GET_INFOS",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.BILL_LOOKED_UP,
                true,
                false,
                false,
                null));
        // Recherche titulaire (ajout bénéficiaire) — fusionne nom / téléphone / compte dans le contexte
        register(ProviderCallOperationSpec.of(
                "INFOCLIENT",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.NONE));
        register(ProviderCallOperationSpec.of(
                "INFO_CLIENT",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.NONE));
        register(ProviderCallOperationSpec.of(
                "GET_BALANCE",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.NONE));
        register(ProviderCallOperationSpec.of(
                "CONSULTATIONSOLDE",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.NONE));
        register(new ProviderCallOperationSpec(
                "PAY_BILL",
                ProviderInvokeRail.BILLER,
                ProviderCallSuccessHandler.SUCCESS_TERMINAL,
                false,
                false,
                false,
                "billProviderReference"));
        register(new ProviderCallOperationSpec(
                "GENERATE_OTP",
                ProviderInvokeRail.PAYER,
                ProviderCallSuccessHandler.OTP_REQUIRED,
                false,
                false,
                false,
                null));
        register(new ProviderCallOperationSpec(
                "CONFIRM_PAYMENT",
                ProviderInvokeRail.PAYER,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE,
                false,
                false,
                true,
                "walletProviderReference"));
        register(ProviderCallOperationSpec.of(
                "TOPUP",
                ProviderInvokeRail.PAYER,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE));
        register(ProviderCallOperationSpec.of(
                "EXECUTE_TOPUP",
                ProviderInvokeRail.PAYER,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE));
        register(new ProviderCallOperationSpec(
                "CREATE_PAYMENT",
                ProviderInvokeRail.PAYER,
                ProviderCallSuccessHandler.REDIRECT_OR_WAIT,
                false,
                false,
                false,
                "walletProviderReference"));
        register(ProviderCallOperationSpec.of(
                "TRANSFER_EXECUTE",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE));
        register(ProviderCallOperationSpec.of(
                "TRANSFERT",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE));
        register(ProviderCallOperationSpec.of(
                "EXECUTE_TRANSFER",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE));
        register(ProviderCallOperationSpec.of(
                "EXECUTE_RTGS",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE));
        register(ProviderCallOperationSpec.of(
                "EXECUTE_SWIFT",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE));
        register(ProviderCallOperationSpec.of(
                "EXECUTE_CASHOUT",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE));
        register(ProviderCallOperationSpec.of(
                "EXECUTE_CASHIN",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE));
        register(new ProviderCallOperationSpec(
                "LINK",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE,
                false,
                false,
                true,
                "linkProviderReference"));
        register(ProviderCallOperationSpec.of(
                "EXECUTE_LINK",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE));
        register(ProviderCallOperationSpec.of(
                "REGISTER",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE));
        register(new ProviderCallOperationSpec(
                "UNLINK",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE,
                false,
                false,
                false,
                "linkProviderReference"));
        register(ProviderCallOperationSpec.of(
                "EXECUTE_UNLINK",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE));
        register(new ProviderCallOperationSpec(
                "REMITTANCE",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE,
                false,
                false,
                false,
                "remittanceReference"));
        register(ProviderCallOperationSpec.of(
                "EXECUTE_REMITTANCE",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE));
        register(ProviderCallOperationSpec.of(
                "GET_QUOTE",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.NONE));
        register(ProviderCallOperationSpec.of(
                "QUOTE",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.NONE));
        register(new ProviderCallOperationSpec(
                "PAYOUT",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE,
                false,
                false,
                false,
                "payoutReference"));
        register(ProviderCallOperationSpec.of(
                "EXECUTE_PAYOUT",
                ProviderInvokeRail.PRIMARY,
                ProviderCallSuccessHandler.CONFIRM_OR_CONTINUE));
    }

    private void register(ProviderCallOperationSpec spec) {
        byOperation.put(normalize(spec.operation()), spec);
    }

    public ProviderCallOperationSpec resolve(String operationCode) {
        if (operationCode == null || operationCode.isBlank()) {
            return defaultSpec(operationCode);
        }
        return byOperation.getOrDefault(normalize(operationCode), defaultSpec(operationCode));
    }

    /**
     * Fallback activité BPMN → code opération (si binding catalogue / variable absents).
     */
    public String defaultOperationForActivity(String activityId) {
        if (activityId == null || activityId.isBlank()) {
            return activityId;
        }
        return switch (activityId) {
            case "generateOtp" -> "GENERATE_OTP";
            case "confirmPayment" -> "CONFIRM_PAYMENT";
            case "createPayment", "initProvider" -> "CREATE_PAYMENT";
            case "transferExecute", "EXECUTE_TRANSFER", "TRANSFER_EXECUTE", "EXECUTE_RTGS", "EXECUTE_SWIFT",
                 "EXECUTE_CASHOUT", "EXECUTE_CASHIN" -> "TRANSFERT";
            case "lookupBill" -> "LOOKUP_BILL";
            case "listBills" -> "BILL_LIST";
            case "payBill" -> "PAY_BILL";
            case "calculateFees" -> "CALCULATE_FEES";
            case "getBalance", "GET_BALANCE" -> "GET_BALANCE";
            case "getInfos", "GET_INFOS", "GETINFOS" -> "GETINFOS";
            case "linkAccount", "EXECUTE_LINK", "LINK" -> "LINK";
            case "unlinkAccount", "EXECUTE_UNLINK", "UNLINK" -> "UNLINK";
            case "evaluateApproval", "EVALUATE_APPROVAL" -> "EVALUATE_APPROVAL";
            case "EXECUTE_REMITTANCE", "executeRemittance" -> "REMITTANCE";
            case "GET_QUOTE", "getQuote" -> "GET_QUOTE";
            case "EXECUTE_PAYOUT", "executePayout" -> "PAYOUT";
            default -> activityId;
        };
    }

    public boolean isRegistered(String operationCode) {
        return operationCode != null && !operationCode.isBlank()
                && byOperation.containsKey(normalize(operationCode));
    }

    public List<ProviderCallOperationSpec> all() {
        return byOperation.values().stream()
                .sorted(Comparator.comparing(ProviderCallOperationSpec::operation))
                .toList();
    }

    public boolean isOtpGeneration(String operationCode) {
        return "GENERATE_OTP".equalsIgnoreCase(operationCode);
    }

    private static ProviderCallOperationSpec defaultSpec(String operationCode) {
        String op = operationCode != null ? operationCode : "UNKNOWN";
        return ProviderCallOperationSpec.of(op, ProviderInvokeRail.PRIMARY, ProviderCallSuccessHandler.NONE);
    }

    private static String normalize(String operation) {
        return operation.trim().toUpperCase(Locale.ROOT);
    }
}
