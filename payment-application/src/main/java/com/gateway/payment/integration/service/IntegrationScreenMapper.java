package com.gateway.payment.integration.service;

import com.gateway.payment.payment.api.dto.FormFieldDto;
import com.gateway.payment.payment.api.dto.NextActionDto;
import com.gateway.payment.payment.api.dto.PaymentResponse;
import com.gateway.payment.integration.api.dto.IntegrationScreenResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mappe {@link PaymentResponse} → {@link IntegrationScreenResponse} (contrat produit pour BFF externes).
 */
public final class IntegrationScreenMapper {

    private IntegrationScreenMapper() {
    }

    public static IntegrationScreenResponse fromPayment(PaymentResponse payment) {
        IntegrationScreenResponse screen = new IntegrationScreenResponse();
        if (payment == null) {
            screen.setType("FAILURE");
            screen.setMessage("Réponse paiement vide");
            return screen;
        }

        NextActionDto next = payment.getNextAction();
        String type = next != null && next.getType() != null ? next.getType().name() : null;
        if (type == null) {
            String status = payment.getStatus() != null ? payment.getStatus().name() : "";
            if ("SUCCEEDED".equals(status) || "COMPLETED".equals(status) || "SUCCESS".equals(status)) {
                type = "SUCCESS";
            } else if ("FAILED".equals(status) || "CANCELLED".equals(status)) {
                type = "FAILURE";
            } else {
                type = "FORM";
            }
        }
        screen.setType(type.toUpperCase());
        screen.setTitle(payment.getCatalogServiceCode());
        if (next != null) {
            screen.setFormKey(next.getFormKey());
            screen.setFormSchema(next.getFormSchema());
            screen.setMessage(firstNonBlank(next.getMessage(), payment.getErrorMessage()));
            screen.setOtpLength(next.getOtpLength());
            screen.setUrl(next.getUrl());
            screen.setPollAfterSeconds(next.getPollAfterSeconds());
            screen.setFields(mapFields(next.getFields()));
        } else {
            screen.setMessage(payment.getErrorMessage());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        put(data, "amount", payment.getAmount());
        put(data, "currency", payment.getCurrency());
        put(data, "phoneNumber", payment.getPhoneNumber());
        put(data, "customerReference", payment.getCustomerReference());
        put(data, "provider", payment.getProvider());
        if (next != null && next.getData() != null) {
            for (Map.Entry<String, Object> e : next.getData().entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    data.put(e.getKey(), e.getValue());
                }
            }
        }
        screen.setData(data);

        Map<String, Object> meta = new LinkedHashMap<>();
        put(meta, "paymentId", payment.getPaymentId());
        put(meta, "status", payment.getStatus() != null ? payment.getStatus().name() : null);
        put(meta, "currentStep", payment.getCurrentStep());
        put(meta, "catalogServiceCode", payment.getCatalogServiceCode());
        put(meta, "journeyCode", payment.getJourneyCode());
        put(meta, "accessChannel", payment.getAccessChannel());
        put(meta, "channel", payment.getAccessChannel());
        put(meta, "errorCode", payment.getErrorCode());
        put(meta, "formKey", screen.getFormKey());
        screen.setMeta(meta);

        String upper = screen.getType();
        if ("ENTER_OTP".equals(upper)) {
            screen.setActions(List.of(Map.of(
                    "id", "SUBMIT_OTP",
                    "label", "Valider OTP",
                    "style", "primary"
            )));
        } else if ("FORM".equals(upper) || "DISPLAY_MESSAGE".equals(upper)) {
            screen.setActions(List.of(Map.of(
                    "id", "SUBMIT_FORM",
                    "label", "Continuer",
                    "style", "primary"
            )));
        }

        return screen;
    }

    private static List<Map<String, Object>> mapFields(List<FormFieldDto> fields) {
        if (fields == null || fields.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (FormFieldDto f : fields) {
            if (f == null) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            put(m, "name", f.getName());
            put(m, "label", f.getLabel());
            put(m, "type", f.getType());
            put(m, "required", f.getRequired());
            put(m, "placeholder", f.getPlaceholder());
            put(m, "pattern", f.getPattern());
            if (f.getOptions() != null && !f.getOptions().isEmpty()) {
                m.put("options", f.getOptions());
            }
            out.add(m);
        }
        return out;
    }

    private static void put(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }
}
