package com.gateway.payment.approval.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.approval.model.ApprovalPolicy;
import com.gateway.payment.approval.repository.ApprovalProfileRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ApprovalCatalogSupportTest {

    @Test
    void parsePolicy_readsNewShape() {
        ApprovalCatalogSupport support = new ApprovalCatalogSupport(
                mock(ApprovalProfileRepository.class), new ObjectMapper());
        Map<String, Object> cfg = Map.of(
                "approvalPolicy", Map.of(
                        "code", "TRANSFER_APPROVAL",
                        "enabled", true,
                        "dmnDecisionKey", "decision_TransferApproval",
                        "initiatorCanApprove", false,
                        "sameUserAcrossLevelsAllowed", false,
                        "criticalFields", List.of("amount", "currency")
                )
        );
        ApprovalPolicy policy = support.parsePolicy(cfg);
        assertTrue(policy.enabled());
        assertEquals("TRANSFER_APPROVAL", policy.code());
        assertEquals("decision_TransferApproval", policy.dmnDecisionKey());
        assertFalse(policy.initiatorCanApprove());
        assertEquals(2, policy.criticalFields().size());
    }

    @Test
    void parsePolicy_disabledWhenMissing() {
        ApprovalCatalogSupport support = new ApprovalCatalogSupport(
                mock(ApprovalProfileRepository.class), new ObjectMapper());
        ApprovalPolicy policy = support.parsePolicy(Map.of());
        assertFalse(policy.enabled());
    }
}
