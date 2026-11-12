package com.gateway.payment.common.domain;

import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentStatusTransitionGuardTest {

    @Test
    void terminalStatesAreImmutable() {
        assertTrue(PaymentStatusTransitionGuard.isTerminal(PaymentStatus.SUCCESS));
        assertTrue(PaymentStatusTransitionGuard.isTerminal(PaymentStatus.FAILED));
        assertFalse(PaymentStatusTransitionGuard.canTransition(PaymentStatus.SUCCESS, PaymentStatus.FAILED));
        assertFalse(PaymentStatusTransitionGuard.canTransition(PaymentStatus.FAILED, PaymentStatus.SUCCESS));
    }

    @Test
    void applyStatusIgnoresOverwriteOnTerminal() {
        PaymentTransaction tx = base(PaymentStatus.SUCCESS);
        assertFalse(tx.applyStatus(PaymentStatus.FAILED));
        assertEquals(PaymentStatus.SUCCESS, tx.getStatus());
    }

    @Test
    void applyStatusAllowsInitiatedToProcessing() {
        PaymentTransaction tx = base(PaymentStatus.INITIATED);
        assertTrue(tx.applyStatus(PaymentStatus.PROCESSING));
        assertEquals(PaymentStatus.PROCESSING, tx.getStatus());
    }

    @Test
    void applyStatusRejectsIllegalMidFlow() {
        PaymentTransaction tx = base(PaymentStatus.CREATED);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> tx.applyStatus(PaymentStatus.SUCCESS));
        assertEquals("INVALID_STATUS_TRANSITION", ex.getCode());
        assertEquals(PaymentStatus.CREATED, tx.getStatus());
    }

    @Test
    void hydrateBypassesGuard() {
        PaymentTransaction tx = base(PaymentStatus.FAILED);
        tx.hydrateStatus(PaymentStatus.UNKNOWN);
        assertEquals(PaymentStatus.UNKNOWN, tx.getStatus());
    }

    private static PaymentTransaction base(PaymentStatus status) {
        return PaymentTransaction.builder()
                .paymentId("PAY-TEST")
                .providerCode("TEST")
                .amount(BigDecimal.ONE)
                .currency("XOF")
                .status(status)
                .build();
    }
}
