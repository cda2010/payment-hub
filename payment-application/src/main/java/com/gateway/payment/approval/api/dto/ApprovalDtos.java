package com.gateway.payment.approval.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ApprovalDtos {

    private ApprovalDtos() {
    }

    public record InboxItem(
            UUID stepInstanceId,
            UUID approvalInstanceId,
            String paymentId,
            String profileCode,
            int levelNumber,
            int requiredApprovals,
            int collectedApprovals,
            String candidateGroups,
            String initiatedBy,
            BigDecimal amount,
            String currency,
            String paymentStatus,
            Instant createdAt
    ) {
    }

    public record DecisionView(
            UUID id,
            String approverId,
            String approverRole,
            String outcome,
            String comment,
            Instant decidedAt
    ) {
    }

    public record StepView(
            UUID id,
            int levelNumber,
            String status,
            int requiredApprovals,
            int collectedApprovals,
            String candidateGroups,
            String bpmTaskDefinitionKey,
            List<DecisionView> decisions
    ) {
    }

    public record Detail(
            UUID approvalInstanceId,
            String paymentId,
            String profileCode,
            String policyCode,
            String strategy,
            String status,
            Integer currentLevel,
            int totalLevels,
            String initiatedBy,
            Instant createdAt,
            Instant completedAt,
            BigDecimal amount,
            String currency,
            String paymentStatus,
            String rejectionComment,
            List<StepView> steps
    ) {
    }

    public record SimulateRequest(
            BigDecimal amount,
            String currency,
            String catalogServiceCode,
            String rail,
            String customerType,
            String agencyCode
    ) {
    }

    public record SimulateResponse(
            String profileCode,
            String strategy,
            int levelCount,
            List<SimulateLevel> levels,
            String source
    ) {
    }

    public record SimulateLevel(
            int level,
            int requiredApprovals,
            List<String> assignments
    ) {
    }

    public record ProfileRequest(
            String code,
            String name,
            String description,
            String definitionJson,
            Boolean actif
    ) {
    }

    public record ProfileResponse(
            UUID id,
            String code,
            String name,
            String description,
            String definitionJson,
            boolean actif,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
