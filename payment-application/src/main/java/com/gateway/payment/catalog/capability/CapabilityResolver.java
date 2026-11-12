package com.gateway.payment.catalog.capability;

import com.gateway.payment.catalog.casting.CatalogBindingDefinition;
import com.gateway.payment.catalog.casting.CatalogBindingKind;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Optional;

/**
 * Résout le code capability depuis le binding catalogue, sinon par inférence
 * (activityId / operation) — compat produits sans {@code bindings[].capability}.
 */
@Component
public class CapabilityResolver {

    public Optional<String> resolve(String step, CatalogBindingDefinition binding) {
        if (binding != null && StringUtils.hasText(binding.capability())) {
            return Optional.of(binding.capability().trim().toUpperCase(Locale.ROOT));
        }
        if (binding != null && StringUtils.hasText(binding.operation())) {
            Optional<String> fromOp = CapabilityOperationMapping.capabilityForOperation(binding.operation());
            if (fromOp.isPresent() && !isSoftProviderMapping(binding, fromOp.get())) {
                return fromOp;
            }
        }
        return inferFromStep(step);
    }

    /**
     * Un binding SOFT est un contrôle local : le router vers une capability qui
     * appelle le fournisseur ferait échouer l'étape chez les providers qui
     * n'exposent pas l'opération (VALIDATE_ACCOUNT → GETINFOS). Seuls
     * {@code kind: INVOKE} ou une {@code capability} explicite déclenchent un appel.
     */
    private static boolean isSoftProviderMapping(CatalogBindingDefinition binding, String capability) {
        return binding.kind() == CatalogBindingKind.SOFT
                && CapabilityOperationMapping.isProviderCapability(capability);
    }

    /**
     * Résolution pour le delegate provider : capability explicite, sinon opération connue, sinon step.
     */
    public Optional<String> resolveProvider(String step, CatalogBindingDefinition binding, String operationCode) {
        if (binding != null && StringUtils.hasText(binding.capability())) {
            return Optional.of(binding.capability().trim().toUpperCase(Locale.ROOT));
        }
        Optional<String> fromOp = CapabilityOperationMapping.capabilityForOperation(operationCode);
        if (fromOp.isPresent()) {
            return fromOp;
        }
        if (binding != null && StringUtils.hasText(binding.operation())) {
            fromOp = CapabilityOperationMapping.capabilityForOperation(binding.operation());
            if (fromOp.isPresent()) {
                return fromOp;
            }
        }
        return inferFromStep(step);
    }

    public Optional<String> inferFromStep(String step) {
        if (!StringUtils.hasText(step)) {
            return Optional.empty();
        }
        String s = step.trim().toUpperCase(Locale.ROOT);
        return switch (s) {
            case "CALCULATE_FEES", "GET_FEES" -> Optional.of(CapabilityCodes.CALCULATE_FEES);
            case "EVALUATE_RULES", "CHECK_RULES", "EVALUATE_LIMITS" -> Optional.of(CapabilityCodes.EVALUATE_RULES);
            case "NOTIFY" -> Optional.of(CapabilityCodes.NOTIFY);
            case "GENERATE_VOUCHER", "GENERATE_CODE" -> Optional.of(CapabilityCodes.GENERATE_CODE);
            case "VALIDATE_AMOUNT", "CHECK_AMOUNT" -> Optional.of(CapabilityCodes.VALIDATE_AMOUNT);
            case "VALIDATE_ACCOUNT", "CHECK_ACCOUNT" -> Optional.of(CapabilityCodes.VALIDATE_ACCOUNT);
            case "APPROVAL_RETURNED", "RETURN_TO_INITIATOR", "APPROVAL_REJECTED" ->
                    Optional.of(CapabilityCodes.APPROVAL_OUTCOME);
            case "GENERATEOTP", "GENERATE_OTP" -> Optional.of(CapabilityCodes.GENERATE_OTP);
            case "LOOKUPBILL", "LOOKUP_BILL" -> Optional.of(CapabilityCodes.GET_BILLS);
            case "PAYBILL", "PAY_BILL" -> Optional.of(CapabilityCodes.PAY_BILL);
            case "CREATEPAYMENT", "CREATE_PAYMENT", "INITPROVIDER" -> Optional.of(CapabilityCodes.CREATE_PAYMENT);
            case "CONFIRMPAYMENT", "CONFIRM_PAYMENT" -> Optional.of(CapabilityCodes.CONFIRM_PAYMENT);
            case "TRANSFEREXECUTE", "EXECUTE_TRANSFER", "TRANSFER_EXECUTE",
                 "EXECUTE_RTGS", "EXECUTE_SWIFT" ->
                    Optional.of(CapabilityCodes.EXECUTE_TRANSFER);
            case "EXECUTE_CASHOUT" -> Optional.of(CapabilityCodes.EXECUTE_CASHOUT);
            case "EXECUTE_CASHIN" -> Optional.of(CapabilityCodes.EXECUTE_CASHIN);
            case "EVALUATE_APPROVAL", "EVALUATEAPPROVAL" -> Optional.of(CapabilityCodes.EVALUATE_APPROVAL);
            case "RESOLVE_DOCUMENTS", "RESOLVEDOCUMENTS", "EVALUATE_DOCUMENTS" ->
                    Optional.of(CapabilityCodes.RESOLVE_DOCUMENTS);
            case "VALIDATE_DOCUMENTS", "VALIDATEDOCUMENTS", "CHECK_DOCUMENTS" ->
                    Optional.of(CapabilityCodes.VALIDATE_DOCUMENTS);
            case "GET_QUOTE", "GETQUOTE", "QUOTE" -> Optional.of(CapabilityCodes.GET_QUOTE);
            case "EXECUTE_REMITTANCE", "EXECUTEREMITTANCE" ->
                    Optional.of(CapabilityCodes.EXECUTE_REMITTANCE);
            case "LOOKUP_REMITTANCE", "LOOKUPREMITTANCE" ->
                    Optional.of(CapabilityCodes.LOOKUP_REMITTANCE);
            case "VALIDATE_PAYOUT", "VALIDATEPAYOUT" -> Optional.of(CapabilityCodes.VALIDATE_PAYOUT);
            case "EXECUTE_PAYOUT", "EXECUTEPAYOUT" -> Optional.of(CapabilityCodes.EXECUTE_PAYOUT);
            case "COMPLETE_PAYOUT", "COMPLETEPAYOUT" -> Optional.of(CapabilityCodes.COMPLETE_PAYOUT);
            case "GET_BALANCE", "GETBALANCE", "CHECK_BALANCE", "BALANCE_CHECK", "CONSULTATIONSOLDE" ->
                    Optional.of(CapabilityCodes.GET_BALANCE);
            case "GET_INFOS", "GETINFOS" -> Optional.of(CapabilityCodes.GET_INFOS);
            case "INFO_CLIENT", "INFOCLIENT", "INFO_CLIENT_COMPTE" -> Optional.of(CapabilityCodes.INFO_CLIENT);
            case "SAVE_TRANSFER_BENEFICIARY", "SAVE_BENEFICIARY" ->
                    Optional.of(CapabilityCodes.SAVE_TRANSFER_BENEFICIARY);
            case "EXECUTE_LINK", "LINKACCOUNT", "LINK_ACCOUNT" -> Optional.of(CapabilityCodes.EXECUTE_LINK);
            case "EXECUTE_UNLINK", "UNLINKACCOUNT", "UNLINK_ACCOUNT" -> Optional.of(CapabilityCodes.EXECUTE_UNLINK);
            default -> {
                // camelCase BPMN activity ids
                if ("generateOtp".equals(step)) {
                    yield Optional.of(CapabilityCodes.GENERATE_OTP);
                }
                if ("lookupBill".equals(step)) {
                    yield Optional.of(CapabilityCodes.GET_BILLS);
                }
                if ("listBills".equals(step)) {
                    yield Optional.of(CapabilityCodes.GET_BILLS);
                }
                if ("calculateFees".equals(step)) {
                    yield Optional.of(CapabilityCodes.CALCULATE_FEES);
                }
                if ("payBill".equals(step)) {
                    yield Optional.of(CapabilityCodes.PAY_BILL);
                }
                if ("createPayment".equals(step) || "initProvider".equals(step)) {
                    yield Optional.of(CapabilityCodes.CREATE_PAYMENT);
                }
                if ("confirmPayment".equals(step)) {
                    yield Optional.of(CapabilityCodes.CONFIRM_PAYMENT);
                }
                if ("linkAccount".equals(step) || "EXECUTE_LINK".equals(step)) {
                    yield Optional.of(CapabilityCodes.EXECUTE_LINK);
                }
                if ("unlinkAccount".equals(step)) {
                    yield Optional.of(CapabilityCodes.EXECUTE_UNLINK);
                }
                if ("transferExecute".equals(step) || "evaluateApproval".equals(step)) {
                    yield Optional.of("transferExecute".equals(step)
                            ? CapabilityCodes.EXECUTE_TRANSFER
                            : CapabilityCodes.EVALUATE_APPROVAL);
                }
                if ("resolveDocuments".equals(step) || "evaluateDocuments".equals(step)) {
                    yield Optional.of(CapabilityCodes.RESOLVE_DOCUMENTS);
                }
                if ("validateDocuments".equals(step)) {
                    yield Optional.of(CapabilityCodes.VALIDATE_DOCUMENTS);
                }
                if ("getBalance".equals(step) || "checkBalance".equals(step)) {
                    yield Optional.of(CapabilityCodes.GET_BALANCE);
                }
                if ("getQuote".equals(step)) {
                    yield Optional.of(CapabilityCodes.GET_QUOTE);
                }
                if ("executeRemittance".equals(step)) {
                    yield Optional.of(CapabilityCodes.EXECUTE_REMITTANCE);
                }
                if ("lookupRemittance".equals(step)) {
                    yield Optional.of(CapabilityCodes.LOOKUP_REMITTANCE);
                }
                if ("validatePayout".equals(step)) {
                    yield Optional.of(CapabilityCodes.VALIDATE_PAYOUT);
                }
                if ("executePayout".equals(step)) {
                    yield Optional.of(CapabilityCodes.EXECUTE_PAYOUT);
                }
                if ("completePayout".equals(step)) {
                    yield Optional.of(CapabilityCodes.COMPLETE_PAYOUT);
                }
                if (s.startsWith("WAIT_")) {
                    yield Optional.of(CapabilityCodes.WAIT);
                }
                yield Optional.empty();
            }
        };
    }
}
