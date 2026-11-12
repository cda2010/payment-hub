package com.gateway.payment.document.service;

import com.gateway.payment.bc.DocumentBcClient;
import com.gateway.payment.bpm.dmn.DmnDocumentRequirementsConfig;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.document.model.DocumentPolicy;
import com.gateway.payment.document.model.DocumentProfile;
import com.gateway.payment.document.model.DocumentRequirement;
import com.gateway.payment.inbox.PartyActionProducer;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.rapidframe.bpm.dmn.DmnEvaluateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Policy → DMN ({@code documentProfileCode}) → profil → variables contexte.
 * Présence : contexte local et/ou coffre {@code payment-documents}.
 */
@Service
public class DocumentEngine {

    private static final Logger log = LoggerFactory.getLogger(DocumentEngine.class);

    private final DocumentCatalogSupport catalogSupport;
    private final DmnEvaluateService dmnEvaluateService;
    private final ObjectProvider<DocumentBcClient> documentsBc;
    private final ObjectProvider<PartyActionProducer> partyActions;

    public DocumentEngine(
            DocumentCatalogSupport catalogSupport,
            ObjectProvider<DmnEvaluateService> dmnEvaluateService,
            ObjectProvider<DocumentBcClient> documentsBc,
            ObjectProvider<PartyActionProducer> partyActions
    ) {
        this.catalogSupport = catalogSupport;
        this.dmnEvaluateService = dmnEvaluateService.getIfAvailable();
        this.documentsBc = documentsBc;
        this.partyActions = partyActions;
    }

    /**
     * Évalue le profil requis et pose les variables BPMN / contexte.
     */
    public ResolvedDocuments resolve(
            PaymentTransaction tx,
            Map<String, Object> context,
            Map<String, Object> processVarsOut
    ) {
        @SuppressWarnings("unchecked")
        Map<String, Object> catalogCfg = context.get("catalogConfiguration") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : Map.of();
        DocumentPolicy policy = catalogSupport.parsePolicy(catalogCfg);

        if (!policy.enabled()) {
            DocumentProfile none = DocumentProfile.none(DmnDocumentRequirementsConfig.PROFILE_NONE);
            applyProfileVars(processVarsOut, context, none);
            return new ResolvedDocuments(policy, none);
        }

        String profileCode = resolveProfileCode(tx, context, policy);
        DocumentProfile profile = catalogSupport.loadProfile(profileCode);
        applyProfileVars(processVarsOut, context, profile);

        log.info("Documents resolved paymentId={} profile={} required={}",
                tx != null ? tx.getPaymentId() : null,
                profile.code(),
                profile.required() != null ? profile.required().size() : 0);
        return new ResolvedDocuments(policy, profile);
    }

    /**
     * Vérifie que chaque pièce {@code required} est présente dans {@code context.documents}.
     */
    public void validate(Map<String, Object> context) {
        foldFlatDocumentKeys(context);

        Object requiredRaw = context.get(DmnDocumentRequirementsConfig.VAR_REQUIRED_DOCS);
        List<DocumentRequirement> required = toRequirements(requiredRaw);
        if (required.isEmpty()) {
            Boolean flag = toBool(context.get(DmnDocumentRequirementsConfig.VAR_DOCUMENTS_REQUIRED));
            if (flag == null || !flag) {
                return;
            }
        }

        Map<String, Object> documents = readDocumentsMap(context);
        List<String> missing = new ArrayList<>();
        for (DocumentRequirement req : required) {
            if (!hasDocument(documents, context, req.code())) {
                missing.add(req.code() + " (" + req.label() + ")");
            }
        }
        if (!missing.isEmpty()) {
            String detail = "Documents requis manquants : " + String.join(", ", missing);
            PartyActionProducer producer = partyActions.getIfAvailable();
            if (producer != null) {
                UUID partyId = PartyActionProducer.partyIdFromContext(context);
                String paymentId = str(context.get("paymentId"));
                if (partyId != null) {
                    producer.documentsMissing(partyId, str(context.get("tenantId")), paymentId, detail);
                }
            }
            throw new BusinessException("DOCUMENTS_MISSING", detail);
        }
    }

    /** Form.io keys {@code documents.ID_FRONT} → map {@code documents.ID_FRONT}. */
    @SuppressWarnings("unchecked")
    private static void foldFlatDocumentKeys(Map<String, Object> context) {
        if (context == null) {
            return;
        }
        Map<String, Object> documents = new LinkedHashMap<>();
        Object existing = context.get(DmnDocumentRequirementsConfig.CTX_DOCUMENTS);
        if (existing instanceof Map<?, ?> m) {
            documents.putAll((Map<String, Object>) m);
        }
        String prefix = DmnDocumentRequirementsConfig.CTX_DOCUMENTS + ".";
        for (Map.Entry<String, Object> e : List.copyOf(context.entrySet())) {
            String key = e.getKey();
            if (key != null && key.startsWith(prefix) && key.length() > prefix.length()) {
                String code = key.substring(prefix.length());
                if (StringUtils.hasText(code) && e.getValue() != null) {
                    documents.put(code, e.getValue());
                }
            }
        }
        if (!documents.isEmpty()) {
            context.put(DmnDocumentRequirementsConfig.CTX_DOCUMENTS, documents);
        }
    }

    private String resolveProfileCode(PaymentTransaction tx, Map<String, Object> context, DocumentPolicy policy) {
        BigDecimal amount = resolveAmount(tx, context, policy.amountField());
        String currency = firstNonBlank(str(context.get("currency")), tx != null ? tx.getCurrency() : null, "XOF");
        String catalogServiceCode = firstNonBlank(
                str(context.get("catalogServiceCode")), "REMITTANCE");
        String providerCode = firstNonBlank(
                str(context.get("providerCode")),
                str(context.get("provider")),
                str(context.get("remittanceProvider")),
                roleProvider(context, "PROVIDER"),
                roleProvider(context, "DESTINATION"));

        Map<String, Object> vars = new HashMap<>();
        vars.put("amount", amount != null ? amount : BigDecimal.ZERO);
        vars.put("currency", currency);
        vars.put("catalogServiceCode", catalogServiceCode);
        vars.put("providerCode", providerCode != null ? providerCode : "");

        if (dmnEvaluateService != null && StringUtils.hasText(policy.dmnDecisionKey())) {
            try {
                Map<String, Object> result = dmnEvaluateService.evaluate(policy.dmnDecisionKey(), vars);
                String code = extractProfileCode(result);
                if (StringUtils.hasText(code)) {
                    return code;
                }
            } catch (Exception e) {
                log.warn("DMN {} failed: {}", policy.dmnDecisionKey(), e.getMessage());
            }
            try {
                Map<String, Object> result = dmnEvaluateService.evaluate(
                        DmnDocumentRequirementsConfig.DECISION_NAME, vars);
                String code = extractProfileCode(result);
                if (StringUtils.hasText(code)) {
                    return code;
                }
            } catch (Exception e) {
                log.warn("DMN name evaluate failed: {}", e.getMessage());
            }
        }
        throw new BusinessException(
                "DOCUMENT_PROFILE_UNRESOLVED",
                "Profil documents introuvable — DMN obligatoire (pas de seuils hardcodés en fallback)");
    }

    private void applyProfileVars(
            Map<String, Object> processVarsOut,
            Map<String, Object> context,
            DocumentProfile profile
    ) {
        boolean required = profile.requiresDocuments();
        List<Map<String, Object>> requiredDocs = toDocMaps(profile.required());
        List<Map<String, Object>> optionalDocs = toDocMaps(profile.optional());

        processVarsOut.put(DmnDocumentRequirementsConfig.VAR_PROFILE_CODE, profile.code());
        processVarsOut.put(DmnDocumentRequirementsConfig.VAR_DOCUMENTS_REQUIRED, required);
        processVarsOut.put(DmnDocumentRequirementsConfig.VAR_REQUIRED_DOCS, requiredDocs);
        processVarsOut.put(DmnDocumentRequirementsConfig.VAR_OPTIONAL_DOCS, optionalDocs);
        processVarsOut.put(DmnDocumentRequirementsConfig.VAR_DOC_APPROVAL, profile.requiresApproval());

        context.putAll(processVarsOut);
    }

    private static List<Map<String, Object>> toDocMaps(List<DocumentRequirement> docs) {
        if (docs == null || docs.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (DocumentRequirement d : docs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", d.code());
            m.put("label", d.label());
            m.put("mimeTypes", d.mimeTypes() != null ? d.mimeTypes() : List.of());
            m.put("maxSizeMb", d.maxSizeMb());
            out.add(m);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static List<DocumentRequirement> toRequirements(Object raw) {
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }
        List<DocumentRequirement> out = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof DocumentRequirement dr) {
                out.add(dr);
                continue;
            }
            if (!(o instanceof Map<?, ?> m)) {
                continue;
            }
            String code = str(m.get("code"));
            if (!StringUtils.hasText(code)) {
                continue;
            }
            String label = firstNonBlank(str(m.get("label")), code);
            List<String> mimeTypes = List.of();
            if (m.get("mimeTypes") instanceof List<?> mt) {
                mimeTypes = mt.stream().map(String::valueOf).toList();
            }
            int maxSize = 5;
            if (m.get("maxSizeMb") instanceof Number n) {
                maxSize = n.intValue();
            }
            out.add(new DocumentRequirement(code, label, mimeTypes, maxSize));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readDocumentsMap(Map<String, Object> context) {
        Object raw = context.get(DmnDocumentRequirementsConfig.CTX_DOCUMENTS);
        if (raw instanceof Map<?, ?> m) {
            return new LinkedHashMap<>((Map<String, Object>) m);
        }
        return Map.of();
    }

    private boolean hasDocument(Map<String, Object> documents, Map<String, Object> context, String code) {
        if (presentInVaultOrUri(documents.get(code))) {
            return true;
        }
        if (presentInVaultOrUri(context.get("documents." + code))) {
            return true;
        }
        if (presentInVaultOrUri(context.get(code))) {
            return true;
        }
        // Liste coffre déjà liée au paiement
        String paymentId = str(context.get("paymentId"));
        DocumentBcClient client = documentsBc.getIfAvailable();
        if (client != null && client.enabled() && StringUtils.hasText(paymentId)) {
            try {
                return client.listByPayment(paymentId).stream()
                        .anyMatch(d -> code.equalsIgnoreCase(d.code()));
            } catch (Exception e) {
                log.debug("Lecture coffre documents échouée: {}", e.getMessage());
            }
        }
        return false;
    }

    private boolean presentInVaultOrUri(Object value) {
        String uri = extractUri(value);
        if (!StringUtils.hasText(uri)) {
            return false;
        }
        // UUID → vérifier dans payment-documents
        try {
            UUID id = UUID.fromString(uri.trim());
            DocumentBcClient client = documentsBc.getIfAvailable();
            if (client != null && client.enabled()) {
                return client.get(id).isPresent();
            }
            return true;
        } catch (IllegalArgumentException ignored) {
            return true; // URL / base64 / storage key locale
        }
    }

    @SuppressWarnings("unchecked")
    private static String extractUri(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String s) {
            return StringUtils.hasText(s) ? s.trim() : null;
        }
        if (value instanceof Map<?, ?> m) {
            for (String key : List.of("uri", "url", "data", "storage", "name", "originalName")) {
                Object v = m.get(key);
                if (v != null && StringUtils.hasText(String.valueOf(v))) {
                    return String.valueOf(v).trim();
                }
            }
            // Form.io file array: [{url, name, ...}]
            Object nested = m.get("0");
            if (nested == null && !m.isEmpty()) {
                Object first = m.values().iterator().next();
                if (first instanceof Map || first instanceof String) {
                    return extractUri(first);
                }
            }
        }
        if (value instanceof List<?> list && !list.isEmpty()) {
            return extractUri(list.get(0));
        }
        return null;
    }

    private BigDecimal resolveAmount(PaymentTransaction tx, Map<String, Object> context, String amountField) {
        if (StringUtils.hasText(amountField)) {
            BigDecimal fromCtx = toDecimal(context.get(amountField));
            if (fromCtx != null) {
                return fromCtx;
            }
        }
        BigDecimal fromAmount = toDecimal(context.get("amount"));
        if (fromAmount != null) {
            return fromAmount;
        }
        BigDecimal debit = toDecimal(context.get("debitAmount"));
        if (debit != null) {
            return debit;
        }
        return tx != null ? tx.getAmount() : null;
    }

    private static String roleProvider(Map<String, Object> context, String role) {
        if (context.get("roleAssignments") instanceof Map<?, ?> roles) {
            Object v = roles.get(role);
            return v != null ? String.valueOf(v).trim() : null;
        }
        return null;
    }

    private static String extractProfileCode(Map<String, Object> result) {
        if (result == null || result.isEmpty()) {
            return null;
        }
        Object v = result.get(DmnDocumentRequirementsConfig.OUT_PROFILE_CODE);
        if (v == null && result.size() == 1) {
            v = result.values().iterator().next();
        }
        return v != null ? String.valueOf(v).trim() : null;
    }

    private static Boolean toBool(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Boolean b) {
            return b;
        }
        return Boolean.parseBoolean(String.valueOf(v));
    }

    private static BigDecimal toDecimal(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        if (v instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return null;
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

    public record ResolvedDocuments(DocumentPolicy policy, DocumentProfile profile) {}
}
