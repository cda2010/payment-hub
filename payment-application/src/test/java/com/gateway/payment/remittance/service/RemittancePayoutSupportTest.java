package com.gateway.payment.remittance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RemittancePayoutSupportTest {

    @Test
    void lookup_findsAvailableSend() throws Exception {
        PaymentTransactionRepository repo = mock(PaymentTransactionRepository.class);
        ObjectMapper om = new ObjectMapper();
        PaymentTransaction send = PaymentTransaction.builder()
                .paymentId("pay-send-1")
                .providerCode("REMITTANCE")
                .serviceType(ServiceTypeCode.TRANSFERT)
                .amount(new BigDecimal("500000"))
                .currency("XOF")
                .status(PaymentStatus.WAITING_CALLBACK)
                .providerReference("REM-001")
                .contextJson(om.writeValueAsString(Map.of(
                        "catalogServiceCode", "REMITTANCE",
                        "remittanceReference", "REM-001",
                        "remittanceStatus", "AVAILABLE_FOR_PAYOUT",
                        "payoutMethod", "CASH_PICKUP",
                        "receiveAmount", 760,
                        "receiveCurrency", "EUR",
                        "beneficiaryName", "Awa Diop"
                )))
                .build();
        when(repo.findRemittanceCandidatesByReference("REM-001")).thenReturn(List.of(send));

        RemittancePayoutSupport support = new RemittancePayoutSupport(repo, om);
        RemittancePayoutSupport.RemittanceSendSnapshot snap = support.lookupAvailable("REM-001");
        assertEquals("CASH_PICKUP", snap.payoutMethod());
        Map<String, Object> projected = support.projectToReceiveContext(snap);
        assertEquals("pay-send-1", projected.get("parentPaymentId"));
        assertEquals("Awa Diop", projected.get("beneficiaryName"));
        assertTrue((Boolean) projected.get("availableForPayout"));
    }

    @Test
    void lookup_rejectsAlreadyPaid() throws Exception {
        PaymentTransactionRepository repo = mock(PaymentTransactionRepository.class);
        ObjectMapper om = new ObjectMapper();
        PaymentTransaction send = PaymentTransaction.builder()
                .paymentId("pay-send-2")
                .providerCode("REMITTANCE")
                .serviceType(ServiceTypeCode.TRANSFERT)
                .amount(BigDecimal.TEN)
                .currency("XOF")
                .status(PaymentStatus.SUCCESS)
                .contextJson(om.writeValueAsString(Map.of(
                        "catalogServiceCode", "REMITTANCE",
                        "remittanceReference", "REM-002",
                        "remittanceStatus", "PAID_OUT",
                        "payoutMethod", "CASH_PICKUP"
                )))
                .build();
        when(repo.findRemittanceCandidatesByReference(anyString())).thenReturn(List.of(send));

        RemittancePayoutSupport support = new RemittancePayoutSupport(repo, om);
        assertThrows(BusinessException.class, () -> support.lookupAvailable("REM-002"));
    }
}
