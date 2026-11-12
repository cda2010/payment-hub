package com.gateway.payment.bpm;

import java.util.Map;

/**
 * Correspondance activité BPMN ↔ code d'étape {@code PaymentFlowStep}
 * (pour nextAction / audit comme PaymentEngine).
 */
public final class BpmActivityStepMapping {

    private static final Map<String, String> OTP_CHECKOUT = Map.of(
            "collectPhone", "COLLECT_PHONE_AND_AMOUNT",
            "generateOtp", "GENERATE_OTP",
            "enterOtp", "WAIT_OTP",
            "confirmPayment", "CONFIRM_PAYMENT"
    );

    private static final Map<String, String> REDIRECT_CHECKOUT = Map.of(
            "collectPayer", "COLLECT_PAYER",
            "initProvider", "INIT_PAYMENT",
            "awaitRedirect", "REDIRECT",
            "awaitCallback", "WAIT_CALLBACK"
    );

    private static final Map<String, String> BILL_PAY = Map.ofEntries(
            Map.entry("collectBill", "COLLECT_BILL"),
            Map.entry("lookupBill", "LOOKUP_BILL"),
            Map.entry("confirmBill", "CONFIRM_BILL"),
            Map.entry("correctBill", "CONFIRM_BILL"),
            Map.entry("approvalCircuit", "EVALUATE_APPROVAL"),
            Map.entry("selectPayProvider", "SELECT_PAY_PROVIDER"),
            Map.entry("walletCheckout", "WALLET_CHECKOUT"),
            Map.entry("walletOtpCheckout", "WALLET_OTP"),
            Map.entry("collectPhone", "COLLECT_PHONE_AND_AMOUNT"),
            Map.entry("generateOtp", "GENERATE_OTP"),
            Map.entry("enterOtp", "WAIT_OTP"),
            Map.entry("confirmPayment", "CONFIRM_PAYMENT"),
            Map.entry("createPayment", "INIT_PAYMENT"),
            Map.entry("waitCallback", "WAIT_CALLBACK"),
            Map.entry("payBill", "PAY_BILL")
    );

    private BpmActivityStepMapping() {
    }

    public static String toStepCode(String processDefinitionKey, String activityId) {
        if (activityId == null) {
            return null;
        }
        if ("providerOtpCheckout".equals(processDefinitionKey)) {
            return OTP_CHECKOUT.getOrDefault(activityId, activityId);
        }
        if ("providerBillPay".equals(processDefinitionKey)) {
            return BILL_PAY.getOrDefault(activityId, activityId);
        }
        if ("providerRedirectCheckout".equals(processDefinitionKey)) {
            return REDIRECT_CHECKOUT.getOrDefault(activityId, activityId);
        }
        return activityId;
    }

    public static String preferredTaskKeyForAction(String action) {
        return switch (action) {
            case "SUBMIT_FORM" -> "collectPhone";
            case "SUBMIT_OTP" -> "enterOtp";
            default -> null;
        };
    }
}
