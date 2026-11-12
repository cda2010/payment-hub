package com.gateway.payment.document.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.document.model.DocumentPolicy;
import com.gateway.payment.document.model.DocumentProfile;
import com.gateway.payment.document.repository.DocumentProfileRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocumentCatalogSupportTest {

    @Test
    void parsePolicy_readsShape() {
        DocumentCatalogSupport support = new DocumentCatalogSupport(
                mock(DocumentProfileRepository.class), new ObjectMapper());
        Map<String, Object> cfg = Map.of(
                "documentPolicy", Map.of(
                        "code", "REMITTANCE_DOCS",
                        "enabled", true,
                        "dmnDecisionKey", "decision_DocumentRequirements",
                        "amountField", "debitAmount",
                        "formKey", "product-document-upload"
                )
        );
        DocumentPolicy policy = support.parsePolicy(cfg);
        assertTrue(policy.enabled());
        assertEquals("REMITTANCE_DOCS", policy.code());
        assertEquals("decision_DocumentRequirements", policy.dmnDecisionKey());
        assertEquals("debitAmount", policy.amountField());
    }

    @Test
    void parsePolicy_disabledWhenMissing() {
        DocumentCatalogSupport support = new DocumentCatalogSupport(
                mock(DocumentProfileRepository.class), new ObjectMapper());
        assertFalse(support.parsePolicy(Map.of()).enabled());
    }

    @Test
    void loadProfile_parsesDefinitionJson() {
        DocumentProfileRepository repo = mock(DocumentProfileRepository.class);
        var entity = com.gateway.payment.document.domain.DocumentProfileEntity.builder()
                .code("BASIC_ID")
                .name("Pièce")
                .definitionJson("""
                        {"required":[{"code":"ID_FRONT","label":"CNI","mimeTypes":["image/jpeg"],"maxSizeMb":5}],
                         "optional":[],"requiresApproval":false}
                        """)
                .actif(true)
                .build();
        when(repo.findByCodeIgnoreCaseAndActifTrue(anyString())).thenReturn(Optional.of(entity));

        DocumentCatalogSupport support = new DocumentCatalogSupport(repo, new ObjectMapper());
        DocumentProfile profile = support.loadProfile("BASIC_ID");
        assertEquals("BASIC_ID", profile.code());
        assertTrue(profile.requiresDocuments());
        assertEquals(1, profile.required().size());
        assertEquals("ID_FRONT", profile.required().get(0).code());
    }
}
