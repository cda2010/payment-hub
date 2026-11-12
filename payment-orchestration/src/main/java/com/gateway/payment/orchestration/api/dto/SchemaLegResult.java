package com.gateway.payment.orchestration.api.dto;

import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.orchestration.domain.ProprieteApi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record SchemaLegResult(
        UUID ligneSchemaEcritureId,
        String stepId,
        Integer ordre,
        String codeFse,
        String typeMethode,
        ProprieteApi apiAUtiliser,
        String providerCode,
        String operationCode,
        boolean success,
        String providerStatus,
        String providerMessage,
        String providerReference,
        PaymentStatus gatewayStatus,
        /** Corps JSON connecteur (ex. {@code bills} pour BILL_LIST). */
        Map<String, Object> responseBody
) {
    public SchemaLegResult {
        // Les connecteurs renvoient des champs à null (ex. numeroCompte), que Map.copyOf refuse.
        responseBody = responseBody != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(responseBody))
                : Map.of();
    }
}
