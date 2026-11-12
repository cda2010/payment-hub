package com.gateway.payment.catalog.document;

import com.gateway.payment.bpm.dmn.DmnDocumentRequirementsConfig;
import com.gateway.payment.catalog.casting.CatalogFormSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Construit un Form.io d'upload à partir de {@code requiredDocs} / {@code optionalDocs} (contexte).
 * <p>
 * Binding : {@code formSource=DOCUMENTS} ou {@code formKey=product-document-upload}.
 */
@Component
public class CatalogDocumentUploadSchemaBuilder {

    public static final String FORM_KEY = "product-document-upload";

    @SuppressWarnings("unchecked")
    public Map<String, Object> build(Map<String, Object> context) {
        String profile = str(context != null ? context.get(DmnDocumentRequirementsConfig.VAR_PROFILE_CODE) : null);
        List<Map<String, Object>> required = docList(context, DmnDocumentRequirementsConfig.VAR_REQUIRED_DOCS);
        List<Map<String, Object>> optional = docList(context, DmnDocumentRequirementsConfig.VAR_OPTIONAL_DOCS);

        String title = "Pièces justificatives";
        if (StringUtils.hasText(profile) && !"NONE".equalsIgnoreCase(profile)) {
            title = title + " (" + profile + ")";
        }

        List<Map<String, Object>> components = new ArrayList<>();

        Map<String, Object> html = new LinkedHashMap<>();
        html.put("type", "htmlelement");
        html.put("key", "documentsIntro");
        html.put("tag", "div");
        html.put("content", "<div class=\"catalog-documents\"><h3>" + escape(title) + "</h3>"
                + "<p>Déposez les documents demandés pour poursuivre.</p></div>");
        components.add(html);

        if (required.isEmpty() && optional.isEmpty()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("type", "htmlelement");
            empty.put("key", "documentsNone");
            empty.put("tag", "p");
            empty.put("content", "Aucun document requis pour cette opération.");
            components.add(empty);
        }

        for (Map<String, Object> doc : required) {
            components.add(fileComponent(doc, true));
        }
        for (Map<String, Object> doc : optional) {
            components.add(fileComponent(doc, false));
        }

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

    private Map<String, Object> fileComponent(Map<String, Object> doc, boolean required) {
        String code = str(doc.get("code"));
        String label = firstNonBlank(str(doc.get("label")), code);
        List<String> mimeTypes = List.of();
        if (doc.get("mimeTypes") instanceof List<?> list) {
            mimeTypes = list.stream().map(String::valueOf).toList();
        }
        int maxMb = 5;
        if (doc.get("maxSizeMb") instanceof Number n) {
            maxMb = n.intValue();
        }

        Map<String, Object> file = new LinkedHashMap<>();
        file.put("type", "file");
        file.put("key", "documents." + code);
        file.put("label", label);
        file.put("input", true);
        file.put("storage", "base64");
        file.put("webcam", false);
        file.put("multiple", false);
        file.put("fileMaxSize", maxMb + "MB");
        if (!mimeTypes.isEmpty()) {
            file.put("filePattern", String.join(",", mimeTypes));
            file.put("fileTypes", mimeTypes);
        }
        if (required) {
            file.put("validate", Map.of("required", true));
        }
        return file;
    }

    public boolean isDocumentsFormKey(String formKey) {
        return FORM_KEY.equalsIgnoreCase(formKey)
                || "catalog-document-upload".equalsIgnoreCase(formKey);
    }

    public boolean shouldRenderDocuments(
            CatalogFormSource formSource,
            String formKey,
            String activityId,
            Map<String, Object> context
    ) {
        if (formSource == CatalogFormSource.DOCUMENTS) {
            return true;
        }
        if (isDocumentsFormKey(formKey)) {
            return true;
        }
        String step = activityId != null ? activityId.trim().toUpperCase() : "";
        if (!("UPLOAD_DOCUMENTS".equals(step)
                || "DOCUMENTS".equals(step)
                || step.endsWith("_DOCUMENTS"))) {
            return false;
        }
        // Uniquement si le profil a déjà été résolu avec des pièces
        Object flag = context != null ? context.get(DmnDocumentRequirementsConfig.VAR_DOCUMENTS_REQUIRED) : null;
        if (flag instanceof Boolean b) {
            return b;
        }
        return flag != null && Boolean.parseBoolean(String.valueOf(flag));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> docList(Map<String, Object> context, String key) {
        if (context == null) {
            return List.of();
        }
        Object raw = context.get(key);
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> m) {
                out.add(new LinkedHashMap<>((Map<String, Object>) m));
            }
        }
        return out;
    }

    private static String str(Object v) {
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() || "null".equals(s) ? null : s;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v.trim();
            }
        }
        return null;
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
