package com.gateway.payment.orchestration.service;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegParamReverserTest {

    @Test
    void reverse_swapsSourceAndDestAccounts() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("CPTSOURCE", "0011000003090");
        params.put("CPTDEST", "BF0600100000111100001");
        params.put("MONTANT", "1000");

        Map<String, String> reversed = LegParamReverser.reverse(params);

        assertEquals("BF0600100000111100001", reversed.get("CPTSOURCE"));
        assertEquals("0011000003090", reversed.get("CPTDEST"));
        assertEquals("1000", reversed.get("MONTANT"));
    }

    @Test
    void reverse_swapsCompteDebiteurAndCredit() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("compteDebiteur", "0011000003090");
        params.put("compteCredit", "BF0600100000111100001");

        Map<String, String> reversed = LegParamReverser.reverse(params);

        assertEquals("BF0600100000111100001", reversed.get("compteDebiteur"));
        assertEquals("0011000003090", reversed.get("compteCredit"));
    }
}
