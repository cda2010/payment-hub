package com.gateway.payment.document.service;

import com.gateway.payment.bpm.dmn.DmnDocumentRequirementsConfig;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.document.model.DocumentProfile;
import com.gateway.payment.document.model.DocumentRequirement;
import com.rapidframe.bpm.dmn.DmnEvaluateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocumentEngineTest {

    @SuppressWarnings("unchecked")
    private static DocumentEngine engine(DocumentCatalogSupport support, ObjectProvider<DmnEvaluateService> dmn) {
        return new DocumentEngine(support, dmn, mock(ObjectProvider.class), mock(ObjectProvider.class));
    }

    @Test
    void resolve_withoutDmnFailsClosed() {
        DocumentCatalogSupport support = mock(DocumentCatalogSupport.class);
        when(support.parsePolicy(any())).thenReturn(
                new com.gateway.payment.document.model.DocumentPolicy(
                        "P", true, "decision_DocumentRequirements", "amount", "product-document-upload"));
        DocumentEngine engine = engine(support, mock(ObjectProvider.class));
        Map<String, Object> context = new HashMap<>();
        context.put("catalogConfiguration", Map.of("documentPolicy", Map.of("enabled", true)));
        context.put("amount", 500000);
        assertThrows(BusinessException.class, () -> engine.resolve(null, context, new HashMap<>()));
    }

    @Test
    void resolve_writesContextVars() {
        DocumentCatalogSupport support = mock(DocumentCatalogSupport.class);
        when(support.parsePolicy(any())).thenReturn(
                new com.gateway.payment.document.model.DocumentPolicy(
                        "P", true, "decision_DocumentRequirements", "amount", "product-document-upload"));
        when(support.loadProfile("BASIC_ID")).thenReturn(new DocumentProfile(
                "BASIC_ID",
                List.of(new DocumentRequirement("ID_FRONT", "CNI", List.of("image/jpeg"), 5)),
                List.of(),
                false
        ));

        @SuppressWarnings("unchecked")
        ObjectProvider<DmnEvaluateService> provider = mock(ObjectProvider.class);
        DmnEvaluateService dmn = mock(DmnEvaluateService.class);
        when(provider.getIfAvailable()).thenReturn(dmn);
        when(dmn.evaluate(anyString(), any())).thenReturn(Map.of("documentProfileCode", "BASIC_ID"));

        DocumentEngine engine = engine(support, provider);
        Map<String, Object> context = new HashMap<>();
        context.put("catalogConfiguration", Map.of("documentPolicy", Map.of("enabled", true)));
        context.put("amount", 500000);
        context.put("currency", "XOF");

        Map<String, Object> vars = new HashMap<>();
        DocumentEngine.ResolvedDocuments resolved = engine.resolve(null, context, vars);

        assertEquals("BASIC_ID", resolved.profile().code());
        assertTrue((Boolean) vars.get(DmnDocumentRequirementsConfig.VAR_DOCUMENTS_REQUIRED));
        assertEquals("BASIC_ID", context.get(DmnDocumentRequirementsConfig.VAR_PROFILE_CODE));
    }

    @Test
    void validate_acceptsFlatFormioKeys() {
        DocumentEngine engine = engine(mock(DocumentCatalogSupport.class), mock(ObjectProvider.class));
        Map<String, Object> context = new HashMap<>();
        context.put(DmnDocumentRequirementsConfig.VAR_DOCUMENTS_REQUIRED, true);
        context.put(DmnDocumentRequirementsConfig.VAR_REQUIRED_DOCS, List.of(
                Map.of("code", "ID_FRONT", "label", "CNI")
        ));
        context.put("documents.ID_FRONT", List.of(Map.of("url", "data:image/jpeg;base64,xx", "name", "cni.jpg")));

        engine.validate(context);
        assertTrue(context.get(DmnDocumentRequirementsConfig.CTX_DOCUMENTS) instanceof Map);
    }

    @Test
    void validate_missingThrows() {
        DocumentEngine engine = engine(mock(DocumentCatalogSupport.class), mock(ObjectProvider.class));
        Map<String, Object> context = new HashMap<>();
        context.put(DmnDocumentRequirementsConfig.VAR_DOCUMENTS_REQUIRED, true);
        context.put(DmnDocumentRequirementsConfig.VAR_REQUIRED_DOCS, List.of(
                Map.of("code", "ID_FRONT", "label", "CNI")
        ));
        assertThrows(BusinessException.class, () -> engine.validate(context));
    }

    @Test
    void resolve_disabledSkips() {
        DocumentCatalogSupport support = mock(DocumentCatalogSupport.class);
        when(support.parsePolicy(any())).thenReturn(
                com.gateway.payment.document.model.DocumentPolicy.disabled());
        DocumentEngine engine = engine(support, mock(ObjectProvider.class));
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> vars = new HashMap<>();
        DocumentEngine.ResolvedDocuments r = engine.resolve(null, context, vars);
        assertEquals("NONE", r.profile().code());
        assertFalse((Boolean) vars.get(DmnDocumentRequirementsConfig.VAR_DOCUMENTS_REQUIRED));
    }
}
