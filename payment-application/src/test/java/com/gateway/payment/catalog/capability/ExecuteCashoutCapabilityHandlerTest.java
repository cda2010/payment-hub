package com.gateway.payment.catalog.capability;

import com.gateway.payment.bc.AliasBcClient;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecuteCashoutCapabilityHandlerTest {

    @Test
    void rejects_missing_source() {
        ExecuteCashoutCapabilityHandler handler = new ExecuteCashoutCapabilityHandler(
                stub(new AtomicReference<>()), disabledAlias());

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("amount", "1000");
        ctx.put("agentNetwork", "CORIS_MONEY");
        ctx.put("destinationAccount", "000000104");

        CapabilityExecutionContext exec = CapabilityExecutionContext.local(
                "pay-1", "EXECUTE_CASHOUT", null, ctx, Map.of());

        BusinessException ex = assertThrows(BusinessException.class, () -> handler.execute(exec, Map.of()));
        assertEquals("CASHOUT_SOURCE_REQUIRED", ex.getCode());
    }

    @Test
    void rejects_missing_destination() {
        ExecuteCashoutCapabilityHandler handler = new ExecuteCashoutCapabilityHandler(
                stub(new AtomicReference<>()), disabledAlias());

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("amount", "1000");
        ctx.put("sourceAccount", "00111000002612");
        ctx.put("agentNetwork", "CORIS_MONEY");

        CapabilityExecutionContext exec = CapabilityExecutionContext.local(
                "pay-1", "EXECUTE_CASHOUT", null, ctx, Map.of());

        BusinessException ex = assertThrows(BusinessException.class, () -> handler.execute(exec, Map.of()));
        assertEquals("CASHOUT_DEST_REQUIRED", ex.getCode());
    }

    @Test
    void debits_and_stays_processing() {
        AtomicReference<String> invokedOp = new AtomicReference<>();
        ExecuteCashoutCapabilityHandler handler = new ExecuteCashoutCapabilityHandler(
                stub(invokedOp), disabledAlias());

        PaymentTransaction tx = PaymentTransaction.builder()
                .amount(new BigDecimal("5000"))
                .build();

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("amount", "5000");
        ctx.put("sourceAccount", "00111000002612");
        ctx.put("agentNetwork", "CORIS_MONEY");
        ctx.put("destinationAccount", "000000104");

        CapabilityExecutionContext exec = CapabilityExecutionContext.local(
                "pay-1", "EXECUTE_CASHOUT", tx, ctx, Map.of());

        CapabilityResult result = handler.execute(exec, Map.of());

        assertEquals("TRANSFERT", invokedOp.get());
        assertEquals(CapabilityResult.TerminalEffect.PROCESSING, result.terminal());
        assertTrue(Boolean.TRUE.equals(ctx.get("cashOutDebited")));
        assertEquals("009", ctx.get("codeService"));
        assertEquals("000000104", ctx.get("destAccount"));
        assertEquals(false, ctx.get("terminalOnConfirm"));
        assertTrue(result.events().stream().anyMatch(e -> "CASHOUT_DEBITED".equals(e.type())));
    }

    private static AliasBcClient disabledAlias() {
        return new AliasBcClient(org.springframework.web.client.RestClient.builder(),
                new com.gateway.payment.bc.GatewayBcProperties(
                        null, null, null, null, null, null, null,
                        true, true, true, true, false, false, false
                )) {
            @Override
            public boolean enabled() {
                return false;
            }
        };
    }

    private static ProviderCapabilitySupport stub(AtomicReference<String> invokedOp) {
        return new ProviderCapabilitySupport(null, null, null, null) {
            @Override
            public CapabilityResult invoke(CapabilityExecutionContext exec, String defaultOperation) {
                invokedOp.set(defaultOperation);
                return CapabilityResult.provider(
                        List.of(new CapabilityResult.CapabilityEvent("API_CALL", "ok")),
                        Map.of("providerSuccess", true),
                        CapabilityResult.TerminalEffect.SUCCESS,
                        null);
            }
        };
    }
}
