package com.gateway.payment.catalog.omnicanal;

import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.provider.ProviderExecutionResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OmnicanalCatalogInvocationServiceTest {

    @Test
    void toTypeMethode_mapsCatalogOps() {
        assertEquals("GET_FEES", OmnicanalCatalogInvocationService.toTypeMethode("CALCULATE_FEES"));
        assertEquals("CONSULTATIONSOLDE", OmnicanalCatalogInvocationService.toTypeMethode("GET_BALANCE"));
        assertEquals("GENERATE_OTP", OmnicanalCatalogInvocationService.toTypeMethode("GENERATE_OTP"));
        assertEquals("TRANSFERT", OmnicanalCatalogInvocationService.toTypeMethode("TRANSFER_EXECUTE"));
        assertEquals("GET_STATUS", OmnicanalCatalogInvocationService.toTypeMethode("GET_STATUS"));
    }

    @Test
    void transferVsGenericClassification() {
        CatalogOmnicanalProperties props = new CatalogOmnicanalProperties();
        OmnicanalCatalogInvocationService svc =
                new OmnicanalCatalogInvocationService(props, null, null, null, null, null);

        assertTrue(svc.isTransferOperation("EXECUTE_TRANSFER"));
        assertTrue(svc.isTransferOperation("EXECUTE_CASHOUT"));
        assertTrue(svc.isTransferOperation("TRANSFER_EXECUTE"));
        assertFalse(svc.isTransferOperation("GET_FEES"));

        assertTrue(svc.isGenericOmnicanalOperation("GET_FEES"));
        assertTrue(svc.isGenericOmnicanalOperation("GENERATE_OTP"));
        assertFalse(svc.isGenericOmnicanalOperation("TRANSFER_EXECUTE"));
        assertFalse(svc.isGenericOmnicanalOperation("UNKNOWN_OP_XYZ"));
    }

    @Test
    void externalResult_readsCodeErrAndReference() {
        ProviderExecutionResult ok = ExternalOmnicanalCatalogGateway.toResult(
                Map.of("codeErr", "0", "msg", "ok", "operation", Map.of("referenceExterne", "MOB-1")),
                "GETINFOS", "GETINFOS", "0011", false);
        assertTrue(ok.isSuccess());
        assertEquals("0", ok.getProviderStatus());
        assertEquals("MOB-1", ok.getProviderReference());

        ProviderExecutionResult ko = ExternalOmnicanalCatalogGateway.toResult(
                Map.of("errCode", "-1", "msg", "refus"),
                "GETINFOS", "GETINFOS", "0011", false);
        assertFalse(ko.isSuccess());
        assertEquals("refus", ko.getProviderMessage());

        ProviderExecutionResult solde = ExternalOmnicanalCatalogGateway.toResult(
                Map.of(
                        "errCode", 0,
                        "msg", "Opération terminée avec Succès",
                        "responseEntity", Map.of(
                                "codeErr", "0",
                                "msg", "ok",
                                "text", "<balance><solde>1500.0</solde></balance>")),
                "BALANCE_CHECK", "CONSULTATIONSOLDE", "0011", false);
        assertTrue(solde.isSuccess());
        assertEquals("1500.0", solde.getResponseBody().get("solde"));
        assertEquals("1500.0", solde.getResponseBody().get("balance"));

        ProviderExecutionResult otp = ExternalOmnicanalCatalogGateway.toResult(
                Map.of("errCode", 10, "msg", "Code OTP requis avant l'envoi au partenaire"),
                "PAIEMENT", "PAIEMENT", "0011", false);
        assertFalse(otp.isSuccess());
        assertEquals(com.gateway.payment.common.domain.PaymentStatus.OTP_REQUIRED, otp.getGatewayStatus());

        ProviderExecutionResult pending = ExternalOmnicanalCatalogGateway.toResult(
                Map.of("errCode", 20, "msg", "Ordre accepté, en attente de confirmation du partenaire"),
                "PAIEMENT", "PAIEMENT", "0011", false);
        assertFalse(pending.isSuccess());
        assertEquals(com.gateway.payment.common.domain.PaymentStatus.WAITING_CALLBACK, pending.getGatewayStatus());
    }

    @Test
    void cashout_usesWithdrawalServiceAndAgentAccount() {
        PaymentTransaction tx = PaymentTransaction.builder().currentStep("EXECUTE_CASHOUT").build();
        assertEquals("009", ExternalOmnicanalCatalogGateway.serviceCode(tx, Map.of()));
        assertEquals("005", ExternalOmnicanalCatalogGateway.serviceCode(null, Map.of()));
        assertEquals("004", ExternalOmnicanalCatalogGateway.serviceCode(
                tx, Map.of("codeService", "004")));
        assertEquals("000000104", ExternalOmnicanalCatalogGateway.beneficiaryAccount(
                Map.of("destinationAccount", "000000104")));
    }

    @Test
    void cashIn_debitsAgentFloatAndCreditsCustomerAccount() {
        PaymentTransaction tx = PaymentTransaction.builder().currentStep("CONFIRM").build();
        assertEquals("003", ExternalOmnicanalCatalogGateway.serviceCode(
                tx, Map.of("productCode", "CASH_IN")));
        String[] accounts = ExternalOmnicanalCatalogGateway.transferAccounts("003", Map.of(
                "agentAccount", "000000104",
                "destinationAccount", "0011000003090"));
        assertEquals("000000104", accounts[0]);
        assertEquals("0011000003090", accounts[1]);
    }
}
