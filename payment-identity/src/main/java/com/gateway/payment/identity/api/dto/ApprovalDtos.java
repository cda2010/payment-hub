package com.gateway.payment.identity.api.dto;

import com.gateway.payment.identity.domain.IdentityApprovalAssignee;
import com.gateway.payment.identity.domain.IdentityApprovalPolicy;
import com.gateway.payment.identity.domain.IdentityApprovalRule;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ApprovalDtos {
    private ApprovalDtos() {}

    public record ApprovalRuleDto(
            String code,
            String label,
            int position,
            Long totalAmountLte,
            Integer lineCountLte,
            Long maxLineAmountLte,
            boolean elseRule,
            String profile
    ) {
        public static ApprovalRuleDto from(IdentityApprovalRule r) {
            return new ApprovalRuleDto(
                    r.getCode(), r.getLabel(), r.getPosition(),
                    r.getTotalAmountLte(), r.getLineCountLte(), r.getMaxLineAmountLte(),
                    r.isElseRule(), r.getProfile());
        }
    }

    public record ApprovalAssigneeDto(
            String level,
            String type,
            String value,
            String scope
    ) {
        public static ApprovalAssigneeDto from(IdentityApprovalAssignee a) {
            return new ApprovalAssigneeDto(a.getLevel(), a.getAssigneeType(), a.getAssigneeValue(), a.getAssigneeScope());
        }
    }

    public record ApprovalPolicyDto(
            UUID id,
            UUID orgNodeId,
            String orgCode,
            String productCode,
            boolean enabled,
            boolean override,
            List<ApprovalRuleDto> rules,
            List<ApprovalAssigneeDto> assignees,
            Instant updatedAt
    ) {
        public static ApprovalPolicyDto from(
                IdentityApprovalPolicy p,
                String orgCode,
                List<ApprovalRuleDto> rules,
                List<ApprovalAssigneeDto> assignees
        ) {
            return new ApprovalPolicyDto(
                    p.getId(), p.getOrgNodeId(), orgCode, p.getProductCode(),
                    p.isEnabled(), p.isOverrideFlag(), rules, assignees, p.getUpdatedAt());
        }
    }

    /** Résolution enfant → parent pour un lancement corporate. {@code applied=false} : le produit décide. */
    public record ApprovalResolutionDto(
            boolean applied,
            String profileCode,
            UUID orgNodeId,
            String orgCode,
            String ruleCode,
            List<LevelRoleDto> levels
    ) {
        public static ApprovalResolutionDto none() {
            return new ApprovalResolutionDto(false, null, null, null, null, List.of());
        }
    }

    public record LevelRoleDto(int level, String role) {}

    public record ApprovalPolicyRequest(
            String productCode,
            Boolean enabled,
            Boolean override,
            List<ApprovalRuleDto> rules,
            List<ApprovalAssigneeDto> assignees
    ) {}
}
