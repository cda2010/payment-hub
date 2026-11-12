package com.m2i.omnicanal.presentation.execution;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionGateTest {

    @Test
    void otpHoldsUntilTheCodeArrivesAndAFinishedOrderIsNotSentAgain() {
        assertEquals(ExecutionGate.Action.HOLD_OTP, ExecutionGate.decide("OTP", false, false, null).action());
        assertEquals(ExecutionGate.Action.CALL, ExecutionGate.decide("OTP", false, true, null).action());
        assertEquals(ExecutionGate.Action.CALL, ExecutionGate.decide("OTP", false, true, "EN_ATTENTE_OTP").action());
        assertEquals(ExecutionGate.Action.HOLD_OTP, ExecutionGate.decide("OTP", false, false, "EN_ATTENTE_OTP").action());
        assertEquals(ExecutionGate.Action.REPLAY, ExecutionGate.decide("DIRECT", false, false, "TERMINE").action());
        assertEquals(ExecutionGate.Action.REPLAY, ExecutionGate.decide("ASYNC", false, false, "EN_ATTENTE_CALLBACK").action());
        assertEquals(ExecutionGate.Action.CALL, ExecutionGate.decide("OTP", true, false, null).action());
    }

    @Test
    void sameOrderKeepsTheSameKeyWhenOnlyTheOtpChanges() {
        Map<String, String> first = Map.of("idempotencyKey", "pay-1", "montant", "500");
        Map<String, String> second = Map.of("idempotencyKey", "pay-1", "montant", "500", "otp", "123456");
        assertEquals("pay-1", ExecutionGate.explicitKey(first));
        assertEquals("pay-1", ExecutionGate.explicitKey(second));
        assertFalse(ExecutionGate.hasOtp(first));
        assertTrue(ExecutionGate.hasOtp(second));
        assertEquals(
                ExecutionGate.fingerprint("0001", "PAIEMENT", first),
                ExecutionGate.fingerprint("0001", "PAIEMENT", second));
    }

    @Test
    void consultationIsNotAnOrder() {
        assertTrue(ExecutionGate.readOnly("GET", "PAIEMENT"));
        assertTrue(ExecutionGate.readOnly("POST", "CONSULTATIONSOLDE"));
        assertFalse(ExecutionGate.readOnly("POST", "PAIEMENT"));
    }
}
