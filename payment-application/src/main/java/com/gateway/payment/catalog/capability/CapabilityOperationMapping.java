package com.gateway.payment.catalog.capability;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Correspondance capability (verbe) ↔ opération provider / Omnicanal.
 * Les codes produit catalogue ne figurent pas ici.
 */
public final class CapabilityOperationMapping {

    private static final Map<String, String> CAPABILITY_TO_OPERATION = Map.ofEntries(
            Map.entry(CapabilityCodes.GET_INFOS, "GETINFOS"),
            Map.entry(CapabilityCodes.INFO_CLIENT, "INFOCLIENT"),
            Map.entry(CapabilityCodes.GET_BILLS, "LOOKUP_BILL"),
            Map.entry(CapabilityCodes.GET_BALANCE, "GET_BALANCE"),
            Map.entry(CapabilityCodes.EXECUTE_TOPUP, "CONFIRM_PAYMENT"),
            Map.entry(CapabilityCodes.PAY_BILL, "PAY_BILL"),
                Map.entry(CapabilityCodes.EXECUTE_TRANSFER, "TRANSFERT"),
            Map.entry(CapabilityCodes.EXECUTE_CASHOUT, "TRANSFERT"),
            Map.entry(CapabilityCodes.EXECUTE_CASHIN, "TRANSFERT"),
            Map.entry(CapabilityCodes.CREATE_PAYMENT, "CREATE_PAYMENT"),
            Map.entry(CapabilityCodes.GENERATE_OTP, "GENERATE_OTP"),
            Map.entry(CapabilityCodes.CONFIRM_PAYMENT, "CONFIRM_PAYMENT"),
            Map.entry(CapabilityCodes.EXECUTE_LINK, "LINK"),
            Map.entry(CapabilityCodes.EXECUTE_UNLINK, "UNLINK"),
            Map.entry(CapabilityCodes.EXECUTE_REMITTANCE, "REMITTANCE"),
            Map.entry(CapabilityCodes.EXECUTE_PAYOUT, "PAYOUT")
    );

    private static final Map<String, String> OPERATION_TO_CAPABILITY = Map.ofEntries(
            Map.entry("GETINFOS", CapabilityCodes.GET_INFOS),
            Map.entry("GET_INFOS", CapabilityCodes.GET_INFOS),
            Map.entry("VALIDATE_ACCOUNT", CapabilityCodes.GET_INFOS),
            Map.entry("INFOCLIENT", CapabilityCodes.INFO_CLIENT),
            Map.entry("INFO_CLIENT", CapabilityCodes.INFO_CLIENT),
            Map.entry("INFO_CLIENT_COMPTE", CapabilityCodes.INFO_CLIENT),
            Map.entry("LOOKUP_BILL", CapabilityCodes.GET_BILLS),
            Map.entry("BILL_LIST", CapabilityCodes.GET_BILLS),
            Map.entry("GET_BILLS", CapabilityCodes.GET_BILLS),
            Map.entry("GET_BALANCE", CapabilityCodes.GET_BALANCE),
            Map.entry("CHECK_BALANCE", CapabilityCodes.GET_BALANCE),
            Map.entry("BALANCE_CHECK", CapabilityCodes.GET_BALANCE),
            Map.entry("CONSULTATIONSOLDE", CapabilityCodes.GET_BALANCE),
            Map.entry("TOPUP", CapabilityCodes.EXECUTE_TOPUP),
            Map.entry("EXECUTE_TOPUP", CapabilityCodes.EXECUTE_TOPUP),
            Map.entry("PAY_BILL", CapabilityCodes.PAY_BILL),
            Map.entry("TRANSFERT", CapabilityCodes.EXECUTE_TRANSFER),
            Map.entry("TRANSFER_EXECUTE", CapabilityCodes.EXECUTE_TRANSFER),
            Map.entry("EXECUTE_TRANSFER", CapabilityCodes.EXECUTE_TRANSFER),
            Map.entry("EXECUTE_RTGS", CapabilityCodes.EXECUTE_TRANSFER),
            Map.entry("EXECUTE_SWIFT", CapabilityCodes.EXECUTE_TRANSFER),
            Map.entry("EXECUTE_CASHOUT", CapabilityCodes.EXECUTE_CASHOUT),
            Map.entry("EXECUTE_CASHIN", CapabilityCodes.EXECUTE_CASHIN),
            Map.entry("CREATE_PAYMENT", CapabilityCodes.CREATE_PAYMENT),
            Map.entry("GENERATE_OTP", CapabilityCodes.GENERATE_OTP),
            Map.entry("CONFIRM_PAYMENT", CapabilityCodes.CONFIRM_PAYMENT),
            Map.entry("LINK", CapabilityCodes.EXECUTE_LINK),
            Map.entry("EXECUTE_LINK", CapabilityCodes.EXECUTE_LINK),
            Map.entry("REGISTER", CapabilityCodes.EXECUTE_LINK),
            Map.entry("UNLINK", CapabilityCodes.EXECUTE_UNLINK),
            Map.entry("EXECUTE_UNLINK", CapabilityCodes.EXECUTE_UNLINK),
            Map.entry("REMITTANCE", CapabilityCodes.EXECUTE_REMITTANCE),
            Map.entry("EXECUTE_REMITTANCE", CapabilityCodes.EXECUTE_REMITTANCE),
            Map.entry("GET_QUOTE", CapabilityCodes.GET_QUOTE),
            Map.entry("QUOTE", CapabilityCodes.GET_QUOTE),
            Map.entry("PAYOUT", CapabilityCodes.EXECUTE_PAYOUT),
            Map.entry("EXECUTE_PAYOUT", CapabilityCodes.EXECUTE_PAYOUT),
            Map.entry("LOOKUP_REMITTANCE", CapabilityCodes.LOOKUP_REMITTANCE),
            Map.entry("VALIDATE_PAYOUT", CapabilityCodes.VALIDATE_PAYOUT),
            Map.entry("COMPLETE_PAYOUT", CapabilityCodes.COMPLETE_PAYOUT)
    );

    private CapabilityOperationMapping() {}

    public static Optional<String> operationForCapability(String capability) {
        if (capability == null || capability.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(CAPABILITY_TO_OPERATION.get(capability.trim().toUpperCase(Locale.ROOT)));
    }

    public static Optional<String> capabilityForOperation(String operation) {
        if (operation == null || operation.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(OPERATION_TO_CAPABILITY.get(operation.trim().toUpperCase(Locale.ROOT)));
    }

    public static boolean isProviderCapability(String capability) {
        return operationForCapability(capability).isPresent()
                || CapabilityCodes.EVALUATE_APPROVAL.equalsIgnoreCase(capability);
    }
}
