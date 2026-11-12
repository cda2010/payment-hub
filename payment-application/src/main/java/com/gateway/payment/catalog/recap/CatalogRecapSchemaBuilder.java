package com.gateway.payment.catalog.recap;

import com.gateway.payment.catalog.casting.CatalogFormSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Construit un Form.io de récap à partir de {@code configuration.recap} + contexte transaction.
 * <p>
 * Tout produit peut déclarer une étape UI {@code formSource=RECAP} (ou {@code formKey=product-recap})
 * sans formulaire métier dédié.
 */
@Component
public class CatalogRecapSchemaBuilder {

    public static final String FORM_KEY = "product-recap";

    @SuppressWarnings("unchecked")
    public Map<String, Object> build(Map<String, Object> catalogConfiguration, Map<String, Object> context) {
        Map<String, Object> recap = readRecap(catalogConfiguration);
        String title = str(recap.get("title"));
        if (title == null) {
            title = "Récapitulatif";
        }
        String subtitle = str(recap.get("subtitle"));
        if (subtitle == null) {
            subtitle = "Vérifiez les informations ci-dessous avant de continuer.";
        }
        String confirmField = str(recap.get("confirmField"));
        if (confirmField == null) {
            confirmField = str(recap.get("confirm_field"));
        }
        if (confirmField == null) {
            confirmField = "confirmRecap";
        }
        String confirmLabel = str(recap.get("confirmLabel"));
        if (confirmLabel == null) {
            confirmLabel = str(recap.get("confirm_label"));
        }
        if (confirmLabel == null) {
            confirmLabel = "Je confirme les informations du récapitulatif";
        }

        List<Map<String, Object>> fieldDefs = fieldDefs(recap);
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"catalog-recap\"><h3>").append(escape(title)).append("</h3>");
        html.append("<p>").append(escape(subtitle)).append("</p><ul>");
        int shown = 0;
        for (Map<String, Object> def : fieldDefs) {
            String key = str(def.get("key"));
            if (key == null) {
                continue;
            }
            Object value = resolveValue(context, key, def);
            if (value == null || String.valueOf(value).isBlank()) {
                boolean required = Boolean.parseBoolean(String.valueOf(def.getOrDefault("required", false)));
                if (!required) {
                    continue;
                }
                value = "—";
            }
            String label = str(def.get("label"));
            if (label == null) {
                label = key;
            }
            html.append("<li><strong>").append(escape(label)).append(" :</strong> ")
                    .append(escape(display(value))).append("</li>");
            shown++;
        }
        if (shown == 0) {
            html.append("<li>Aucune donnée de récap disponible — vérifiez puis confirmez.</li>");
        }
        html.append("</ul></div>");

        List<Map<String, Object>> components = new ArrayList<>();
        Map<String, Object> htmlComp = new LinkedHashMap<>();
        htmlComp.put("type", "htmlelement");
        htmlComp.put("key", "recapSummary");
        htmlComp.put("tag", "div");
        htmlComp.put("content", html.toString());
        components.add(htmlComp);

        Map<String, Object> checkbox = new LinkedHashMap<>();
        checkbox.put("type", "checkbox");
        checkbox.put("key", confirmField);
        checkbox.put("label", confirmLabel);
        checkbox.put("input", true);
        checkbox.put("defaultValue", false);
        checkbox.put("validate", Map.of("required", true));
        components.add(checkbox);

        Map<String, Object> submit = new LinkedHashMap<>();
        submit.put("type", "button");
        submit.put("key", "submit");
        submit.put("label", "Submit");
        submit.put("input", true);
        submit.put("hidden", true);
        components.add(submit);

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("display", "form");
        schema.put("components", components);
        return schema;
    }

    public boolean isRecapFormKey(String formKey) {
        return FORM_KEY.equalsIgnoreCase(formKey) || "catalog-recap".equalsIgnoreCase(formKey);
    }

    public boolean isEnabled(Map<String, Object> catalogConfiguration) {
        Map<String, Object> recap = readRecap(catalogConfiguration);
        if (recap.isEmpty()) {
            return false;
        }
        Object enabled = recap.get("enabled");
        if (enabled == null) {
            enabled = recap.get("enable");
        }
        return enabled == null || Boolean.parseBoolean(String.valueOf(enabled));
    }

    /** formSource=RECAP, formKey product-recap, ou étape CONFIRM/RECAP + {@code recap.enabled}. */
    public boolean shouldRenderRecap(
            CatalogFormSource formSource,
            String formKey,
            String activityId,
            Map<String, Object> catalogConfiguration
    ) {
        if (formSource == CatalogFormSource.RECAP) {
            return true;
        }
        if (isRecapFormKey(formKey)) {
            return true;
        }
        if (!isEnabled(catalogConfiguration)) {
            return false;
        }
        String step = activityId != null ? activityId.trim().toUpperCase() : "";
        return "CONFIRM".equals(step)
                || "RECAP".equals(step)
                || step.endsWith("_RECAP")
                || "CONFIRM_TRANSFER".equals(step)
                || "CONFIRM_CASHOUT".equals(step);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readRecap(Map<String, Object> configuration) {
        if (configuration == null) {
            return Map.of();
        }
        Object raw = configuration.get("recap");
        if (raw instanceof Map<?, ?> m) {
            return new LinkedHashMap<>((Map<String, Object>) m);
        }
        return Map.of();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> fieldDefs(Map<String, Object> recap) {
        Object fields = recap.get("fields");
        if (fields instanceof List<?> list && !list.isEmpty()) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> m) {
                    out.add(new LinkedHashMap<>((Map<String, Object>) m));
                } else if (item != null) {
                    out.add(Map.of("key", String.valueOf(item)));
                }
            }
            return out;
        }
        // Défaut générique multi-produits
        return List.of(
                Map.of("key", "catalogServiceCode", "label", "Produit"),
                Map.of("key", "amount", "label", "Montant"),
                Map.of("key", "currency", "label", "Devise"),
                Map.of("key", "fees", "label", "Frais"),
                Map.of("key", "totalDebit", "label", "Total débité"),
                Map.of("key", "sourceRail", "label", "Compte source"),
                Map.of("key", "destRail", "label", "Destination"),
                Map.of("key", "agentNetwork", "label", "Réseau agent"),
                Map.of("key", "phoneNumber", "label", "Téléphone"),
                Map.of("key", "bic", "label", "BIC"),
                Map.of("key", "iban", "label", "IBAN"),
                Map.of("key", "purpose", "label", "Motif"),
                Map.of("key", "provider", "label", "Provider"),
                Map.of("key", "payProvider", "label", "Moyen de paiement")
        );
    }

    private static Object resolveValue(Map<String, Object> context, String key, Map<String, Object> def) {
        if (context == null) {
            return null;
        }
        Object direct = context.get(key);
        if (direct != null) {
            return direct;
        }
        // aliases courants
        return switch (key) {
            case "providerRail" -> first(context, "SOURCE", "sourceRail", "sourceProvider");
            case "sourceRail" -> first(context, "SOURCE", "sourceProvider", "providerRail");
            case "destRail" -> first(context, "DESTINATION", "destinationProvider", "destProvider", "destinationRail");
            case "destinationRail" -> first(context, "DESTINATION", "destRail", "destinationProvider");
            case "provider" -> first(context, "providerCode");
            case "payProvider" -> first(context, "payProviderCode");
            default -> null;
        };
    }

    private static Object first(Map<String, Object> context, String... keys) {
        if (context.get("roleAssignments") instanceof Map<?, ?> roles) {
            for (String key : keys) {
                Object v = roles.get(key);
                if (v != null && StringUtils.hasText(String.valueOf(v))) {
                    return v;
                }
            }
        }
        for (String key : keys) {
            Object v = context.get(key);
            if (v != null && StringUtils.hasText(String.valueOf(v))) {
                return v;
            }
        }
        return null;
    }

    private static String str(Object v) {
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() || "null".equals(s) ? null : s;
    }

    /** Le récap est lu par un client : un montant s'y affiche « 150000 », pas « 150000.0 ». */
    private static String display(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd.stripTrailingZeros().toPlainString();
        }
        if (value instanceof Double || value instanceof Float) {
            return BigDecimal.valueOf(((Number) value).doubleValue()).stripTrailingZeros().toPlainString();
        }
        return String.valueOf(value);
    }

    private static String escape(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
