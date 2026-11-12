package com.gateway.payment.catalog.capability;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetQuoteCapabilityHandlerTest {

    @Test
    void buildsQuoteFromAmountAndFx() {
        SoftRuleCapabilitySupport soft = mock(SoftRuleCapabilitySupport.class);
        when(soft.apply(anyString(), any())).thenReturn(
                new SoftRuleCapabilitySupport.SoftBundle(false, java.util.List.of(), new HashMap<>()));

        GetQuoteCapabilityHandler handler = new GetQuoteCapabilityHandler(soft);
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("amount", new BigDecimal("500000"));
        ctx.put("currency", "XOF");
        ctx.put("receiveCurrency", "EUR");

        Map<String, Object> configuration = Map.of(
                "remittanceQuote", Map.of(
                        "defaultFee", 1500,
                        "ttlMinutes", 15,
                        "fxRates", Map.of("XOF>EUR", 0.00152)
                )
        );

        CapabilityExecutionContext exec = CapabilityExecutionContext.local(
                "pay-1", "GET_QUOTE", null, ctx, Map.of());
        CapabilityResult result = handler.execute(exec, configuration);

        assertTrue(result.handled());
        assertNotNull(ctx.get("quoteId"));
        assertEquals("QUOTED", ctx.get("quoteStatus"));
        assertEquals(new BigDecimal("1500.00"), ctx.get("fees"));
        assertEquals(new BigDecimal("760.00"), ctx.get("receiveAmount"));
        assertEquals(new BigDecimal("501500.00"), ctx.get("totalDebit"));
    }

    @Test
    void failsWithoutConfiguredFxRate() {
        SoftRuleCapabilitySupport soft = mock(SoftRuleCapabilitySupport.class);
        when(soft.apply(anyString(), any())).thenReturn(
                new SoftRuleCapabilitySupport.SoftBundle(false, java.util.List.of(), new HashMap<>()));

        GetQuoteCapabilityHandler handler = new GetQuoteCapabilityHandler(soft);
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("amount", new BigDecimal("500000"));
        ctx.put("currency", "XOF");
        ctx.put("receiveCurrency", "EUR");

        CapabilityExecutionContext exec = CapabilityExecutionContext.local(
                "pay-1", "GET_QUOTE", null, ctx, Map.of());
        assertThrows(com.gateway.payment.common.exception.BusinessException.class,
                () -> handler.execute(exec, Map.of()));
    }
}
