package com.gateway.payment.catalog.capability;

import org.springframework.stereotype.Component;

import java.util.Map;

/** Handler provider générique : un bean Spring par code capability. */
abstract class AbstractProviderCapabilityHandler implements CapabilityHandler {

    private final String code;
    private final String defaultOperation;
    private final ProviderCapabilitySupport providerSupport;

    protected AbstractProviderCapabilityHandler(
            String code,
            String defaultOperation,
            ProviderCapabilitySupport providerSupport
    ) {
        this.code = code;
        this.defaultOperation = defaultOperation;
        this.providerSupport = providerSupport;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String defaultOperation() {
        return defaultOperation;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        return providerSupport.invoke(context, defaultOperation);
    }
}

@Component
class GetInfosCapabilityHandler extends AbstractProviderCapabilityHandler {
    GetInfosCapabilityHandler(ProviderCapabilitySupport support) {
        super(CapabilityCodes.GET_INFOS, "GETINFOS", support);
    }
}

/** Recherche titulaire FSE (wallet téléphone / compte banque) via Omnicanal INFOCLIENT. */
@Component
class InfoClientCapabilityHandler extends AbstractProviderCapabilityHandler {
    InfoClientCapabilityHandler(ProviderCapabilitySupport support) {
        super(CapabilityCodes.INFO_CLIENT, "INFOCLIENT", support);
    }
}

@Component
class GetBillsCapabilityHandler implements CapabilityHandler {
    private final ProviderCapabilitySupport support;

    GetBillsCapabilityHandler(ProviderCapabilitySupport support) {
        this.support = support;
    }

    @Override
    public String code() {
        return CapabilityCodes.GET_BILLS;
    }

    @Override
    public String defaultOperation() {
        return "LOOKUP_BILL";
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        String op = "LOOKUP_BILL";
        if (context.binding() != null && context.binding().operation() != null
                && !context.binding().operation().isBlank()) {
            op = context.binding().operation().trim();
        }
        return support.invoke(context, op);
    }
}

@Component
class GetBalanceCapabilityHandler extends AbstractProviderCapabilityHandler {
    GetBalanceCapabilityHandler(ProviderCapabilitySupport support) {
        super(CapabilityCodes.GET_BALANCE, "GET_BALANCE", support);
    }
}

@Component
class ExecuteTopupCapabilityHandler extends AbstractProviderCapabilityHandler {
    ExecuteTopupCapabilityHandler(ProviderCapabilitySupport support) {
        super(CapabilityCodes.EXECUTE_TOPUP, "CONFIRM_PAYMENT", support);
    }
}

@Component
class PayBillCapabilityHandler extends AbstractProviderCapabilityHandler {
    PayBillCapabilityHandler(ProviderCapabilitySupport support) {
        super(CapabilityCodes.PAY_BILL, "PAY_BILL", support);
    }
}

@Component
class ExecuteTransferCapabilityHandler extends AbstractProviderCapabilityHandler {
    ExecuteTransferCapabilityHandler(ProviderCapabilitySupport support) {
        super(CapabilityCodes.EXECUTE_TRANSFER, "TRANSFERT", support);
    }
}

@Component
class CreatePaymentCapabilityHandler extends AbstractProviderCapabilityHandler {
    CreatePaymentCapabilityHandler(ProviderCapabilitySupport support) {
        super(CapabilityCodes.CREATE_PAYMENT, "CREATE_PAYMENT", support);
    }
}

@Component
class GenerateOtpCapabilityHandler extends AbstractProviderCapabilityHandler {
    GenerateOtpCapabilityHandler(ProviderCapabilitySupport support) {
        super(CapabilityCodes.GENERATE_OTP, "GENERATE_OTP", support);
    }
}

@Component
class ConfirmPaymentCapabilityHandler extends AbstractProviderCapabilityHandler {
    ConfirmPaymentCapabilityHandler(ProviderCapabilitySupport support) {
        super(CapabilityCodes.CONFIRM_PAYMENT, "CONFIRM_PAYMENT", support);
    }
}

@Component
class ExecuteLinkCapabilityHandler extends AbstractProviderCapabilityHandler {
    ExecuteLinkCapabilityHandler(ProviderCapabilitySupport support) {
        super(CapabilityCodes.EXECUTE_LINK, "LINK", support);
    }
}

@Component
class ExecuteUnlinkCapabilityHandler extends AbstractProviderCapabilityHandler {
    ExecuteUnlinkCapabilityHandler(ProviderCapabilitySupport support) {
        super(CapabilityCodes.EXECUTE_UNLINK, "UNLINK", support);
    }
}

@Component
class ExecuteRemittanceCapabilityHandler extends AbstractProviderCapabilityHandler {
    ExecuteRemittanceCapabilityHandler(ProviderCapabilitySupport support) {
        super(CapabilityCodes.EXECUTE_REMITTANCE, "REMITTANCE", support);
    }
}

@Component
class ExecutePayoutCapabilityHandler extends AbstractProviderCapabilityHandler {
    ExecutePayoutCapabilityHandler(ProviderCapabilitySupport support) {
        super(CapabilityCodes.EXECUTE_PAYOUT, "PAYOUT", support);
    }
}
