package com.gateway.payment.document.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.bpm.dmn.DmnDocumentRequirementsConfig;
import com.gateway.payment.document.domain.DocumentProfileEntity;
import com.gateway.payment.document.model.DocumentPolicy;
import com.gateway.payment.document.model.DocumentProfile;
import com.gateway.payment.document.model.DocumentRequirement;
import com.gateway.payment.document.repository.DocumentProfileRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class DocumentCatalogSupport {

    private final DocumentProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    public DocumentCatalogSupport(DocumentProfileRepository profileRepository, ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public DocumentPolicy parsePolicy(Map<String, Object> catalogConfiguration) {
        if (catalogConfiguration == null) {
            return DocumentPolicy.disabled();
        }
        Object raw = catalogConfiguration.get("documentPolicy");
        if (!(raw instanceof Map<?, ?> map)) {
            return DocumentPolicy.disabled();
        }
        boolean enabled = truthy(map.get("enabled"), true);
        String code = str(map.get("code"));
        String dmnKey = firstNonBlank(
                str(map.get("dmnDecisionKey")),
                str(map.get("decisionKey")),
                DmnDocumentRequirementsConfig.DECISION_KEY);
        String amountField = firstNonBlank(str(map.get("amountField")), "amount");
        String formKey = firstNonBlank(str(map.get("formKey")), "product-document-upload");
        return new DocumentPolicy(code, enabled, dmnKey, amountField, formKey);
    }

    public DocumentProfile loadProfile(String profileCode) {
        String code = StringUtils.hasText(profileCode)
                ? profileCode.trim().toUpperCase(Locale.ROOT)
                : DmnDocumentRequirementsConfig.PROFILE_NONE;
        return profileRepository.findByCodeIgnoreCaseAndActifTrue(code)
                .map(this::toProfile)
                .orElseGet(() -> DocumentProfile.none(code));
    }

    public DocumentProfile toProfile(DocumentProfileEntity entity) {
        try {
            Map<String, Object> def = objectMapper.readValue(
                    entity.getDefinitionJson(), new TypeReference<>() {});
            List<DocumentRequirement> required = parseRequirements(def.get("required"));
            List<DocumentRequirement> optional = parseRequirements(def.get("optional"));
            boolean requiresApproval = truthy(def.get("requiresApproval"), false);
            return new DocumentProfile(entity.getCode(), required, optional, requiresApproval);
        } catch (Exception e) {
            return DocumentProfile.none(entity.getCode());
        }
    }

    @SuppressWarnings("unchecked")
    private List<DocumentRequirement> parseRequirements(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<DocumentRequirement> out = new ArrayList<>();
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> m)) {
                continue;
            }
            String code = str(m.get("code"));
            if (!StringUtils.hasText(code)) {
                continue;
            }
            String label = firstNonBlank(str(m.get("label")), code);
            List<String> mimeTypes = parseStringList(m.get("mimeTypes"));
            int maxSizeMb = toInt(m.get("maxSizeMb"), 5);
            out.add(new DocumentRequirement(code.trim(), label, mimeTypes, maxSizeMb));
        }
        return out;
    }

    private static List<String> parseStringList(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (Object o : list) {
            if (o != null && StringUtils.hasText(String.valueOf(o))) {
                out.add(String.valueOf(o).trim());
            }
        }
        return out;
    }

    private static boolean truthy(Object v, boolean defaultValue) {
        if (v == null) {
            return defaultValue;
        }
        if (v instanceof Boolean b) {
            return b;
        }
        return !"false".equalsIgnoreCase(String.valueOf(v).trim());
    }

    private static int toInt(Object v, int defaultValue) {
        if (v == null) {
            return defaultValue;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        try {
            return (int) Double.parseDouble(String.valueOf(v).trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static String str(Object v) {
        return v != null ? String.valueOf(v).trim() : null;
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
}
