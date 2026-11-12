package com.gateway.payment.billing.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.billing.api.dto.ProductFieldDto;
import com.gateway.payment.billing.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductUiSchemaService {

    private final ObjectMapper objectMapper;

    public ProductUiSchemaService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<ProductFieldDto> resolveLookupFields(BillerProduct product) {
        List<ProductFieldDto> configured = read(product.getLookupFieldsJson());
        if (!configured.isEmpty()) {
            return configured;
        }
        return defaultLookupFields(product);
    }

    public List<ProductFieldDto> resolveConfirmFields(BillerProduct product) {
        List<ProductFieldDto> configured = read(product.getConfirmFieldsJson());
        if (!configured.isEmpty()) {
            return configured;
        }
        // Défaut produit : confirmation explicite du montant / facture trouvée
        return List.of(field(
                "confirmPay",
                "Je confirme le montant et le paiement de cette facture",
                "boolean",
                true,
                null,
                null,
                null,
                null,
                null
        ));
    }

    public List<ProductFieldDto> resolveAmountFields(BillerProduct product) {
        List<ProductFieldDto> configured = read(product.getAmountFieldsJson());
        if (!configured.isEmpty()) {
            return configured;
        }
        if (product.getJourneyCode() == JourneyCode.PREPAID
                || product.getAmountPolicy() == AmountPolicy.EDITABLE) {
            return List.of(field("amount", "Montant", "number", true, "5000", null,
                    ProductFieldRole.AMOUNT, null, null));
        }
        return List.of();
    }

    public String writeFields(List<ProductFieldDto> fields) {
        if (fields == null) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(fields);
        } catch (Exception ex) {
            return "[]";
        }
    }

    public List<ProductFieldDto> read(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<ProductFieldDto> defaultLookupFields(BillerProduct product) {
        List<ProductFieldDto> fields = new ArrayList<>();
        LookupMode mode = product.getLookupMode();
        if (mode == LookupMode.SUBSCRIBER || mode == LookupMode.BOTH) {
            fields.add(field("subscriberReference", "Référence abonné", "text", true,
                    "AB-1001", null, ProductFieldRole.SUBSCRIBER, null, null));
        }
        if (mode == LookupMode.BILL_REF || mode == LookupMode.BOTH) {
            fields.add(field("billReference", "N° de facture", "text", true,
                    "FAC-998877", null, ProductFieldRole.BILL_REF, null, null));
        }
        if (product.getJourneyCode() == JourneyCode.PREPAID && fields.isEmpty()) {
            fields.add(field("subscriberReference", "Compteur / abonné", "text", true,
                    "AB-1001", null, ProductFieldRole.SUBSCRIBER, null, null));
        }
        return fields;
    }

    private static ProductFieldDto field(
            String name, String label, String type, boolean required,
            String placeholder, String pattern, ProductFieldRole role,
            List<String> options, String defaultValue
    ) {
        return new ProductFieldDto(name, label, type, required, placeholder, pattern, role, options, defaultValue);
    }
}
