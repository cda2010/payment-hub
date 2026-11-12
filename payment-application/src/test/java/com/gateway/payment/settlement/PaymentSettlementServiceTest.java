package com.gateway.payment.settlement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.bc.CommissionBcClient;
import com.gateway.payment.bc.RulesBcClient;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentSettlementServiceTest {

    @Mock
    private RulesBcClient rules;
    @Mock
    private CommissionBcClient commission;

    @Test
    void successConfirmsEveryHoldOfThePayment() {
        when(rules.enabled()).thenReturn(true);
        when(commission.enabled()).thenReturn(false);
        PaymentSettlementService service = new PaymentSettlementService(rules, commission, new ObjectMapper());
        PaymentTransaction tx = PaymentTransaction.builder()
                .paymentId("PAY-1")
                .providerCode("CASH_IN")
                .amount(new BigDecimal("10000"))
                .currency("XOF")
                .contextJson("{\"ruleReservationId\":\"c2000001-0000-4000-8000-000000000003\"}")
                .build();

        service.onSuccess(tx);

        verify(rules).confirmByTransaction("PAY-1");
        verify(rules, never()).confirm(any());
    }

    @Test
    void failureReleasesEveryHoldOfThePayment() {
        when(rules.enabled()).thenReturn(true);
        PaymentSettlementService service = new PaymentSettlementService(rules, commission, new ObjectMapper());
        PaymentTransaction tx = PaymentTransaction.builder()
                .paymentId("PAY-1")
                .providerCode("CASH_IN")
                .amount(new BigDecimal("10000"))
                .currency("XOF")
                .contextJson("{\"ruleReservationId\":\"c2000001-0000-4000-8000-000000000003\"}")
                .build();

        service.onFailure(tx);

        verify(rules).releaseByTransaction("PAY-1");
        verify(rules, never()).release(any());
    }
}
