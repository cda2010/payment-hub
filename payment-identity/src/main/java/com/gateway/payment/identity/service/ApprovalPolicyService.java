package com.gateway.payment.identity.service;

import com.gateway.payment.identity.api.dto.ApprovalDtos.*;
import com.gateway.payment.identity.domain.IdentityApprovalAssignee;
import com.gateway.payment.identity.domain.IdentityApprovalPolicy;
import com.gateway.payment.identity.domain.IdentityApprovalRule;
import com.gateway.payment.identity.domain.IdentityOrgNode;
import com.gateway.payment.identity.repository.IdentityApprovalAssigneeRepository;
import com.gateway.payment.identity.repository.IdentityApprovalPolicyRepository;
import com.gateway.payment.identity.repository.IdentityApprovalRuleRepository;
import com.gateway.payment.identity.repository.IdentityOrgNodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

/** Politiques d'approbation du portail corporate, rangées sous un nœud d'organisation. */
@Service
public class ApprovalPolicyService {

    private static final Set<String> PROFILES = Set.of(
            "NO_APPROVAL", "L1_CHECKER", "L1_L2", "LEVEL_1", "LEVEL_1_2", "HIGH_VALUE");
    private static final Set<String> LEVELS = Set.of("L1", "L2");
    private static final Set<String> SCOPES = Set.of("ENTITY", "TENANT");

    private final IdentityApprovalPolicyRepository policies;
    private final IdentityApprovalRuleRepository rules;
    private final IdentityApprovalAssigneeRepository assignees;
    private final IdentityOrgNodeRepository orgs;

    public ApprovalPolicyService(
            IdentityApprovalPolicyRepository policies,
            IdentityApprovalRuleRepository rules,
            IdentityApprovalAssigneeRepository assignees,
            IdentityOrgNodeRepository orgs
    ) {
        this.policies = policies;
        this.rules = rules;
        this.assignees = assignees;
        this.orgs = orgs;
    }

    @Transactional(readOnly = true)
    public List<ApprovalPolicyDto> list(String tenantId) {
        String tenant = tenant(tenantId);
        return policies.findByTenantIdOrderByProductCodeAsc(tenant).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ApprovalPolicyDto save(UUID orgNodeId, ApprovalPolicyRequest body) {
        if (body == null || body.productCode() == null || body.productCode().isBlank()) {
            throw new IllegalArgumentException("Le produit est obligatoire.");
        }
        IdentityOrgNode node = orgs.findById(orgNodeId)
                .orElseThrow(() -> new NoSuchElementException("Nœud introuvable"));
        String product = body.productCode().trim().toUpperCase(Locale.ROOT);
        IdentityApprovalPolicy policy = policies.findByOrgNodeIdAndProductCode(orgNodeId, product)
                .orElseGet(() -> IdentityApprovalPolicy.builder()
                        .tenantId(node.getTenantId())
                        .orgNodeId(orgNodeId)
                        .productCode(product)
                        .build());
        policy.setEnabled(body.enabled() == null || body.enabled());
        policy.setOverrideFlag(body.override() == null || body.override());
        policy = policies.save(policy);

        rules.deleteByPolicyId(policy.getId());
        assignees.deleteByPolicyId(policy.getId());
        policies.flush();

        int position = 0;
        for (ApprovalRuleDto rule : body.rules() == null ? List.<ApprovalRuleDto>of() : body.rules()) {
            rules.save(toRule(policy.getId(), rule, position++));
        }
        for (ApprovalAssigneeDto assignee : body.assignees() == null ? List.<ApprovalAssigneeDto>of() : body.assignees()) {
            assignees.save(toAssignee(policy.getId(), assignee));
        }
        return toDto(policy);
    }

    /**
     * Première politique active et prioritaire en remontant le nœud vers ses parents.
     * Le profil renvoyé est un code du catalogue ({@code NO_APPROVAL}, {@code LEVEL_1}, {@code LEVEL_1_2}, {@code HIGH_VALUE}).
     */
    @Transactional(readOnly = true)
    public ApprovalResolutionDto resolve(
            UUID orgNodeId,
            String productCode,
            long amount,
            Integer lineCount,
            Long maxLineAmount
    ) {
        if (orgNodeId == null || productCode == null || productCode.isBlank()) {
            return ApprovalResolutionDto.none();
        }
        String product = productCode.trim().toUpperCase(Locale.ROOT);
        UUID current = orgNodeId;
        for (int guard = 0; current != null && guard < 32; guard++) {
            IdentityOrgNode node = orgs.findById(current).orElse(null);
            if (node == null) {
                return ApprovalResolutionDto.none();
            }
            IdentityApprovalPolicy policy = policies.findByOrgNodeIdAndProductCode(current, product).orElse(null);
            if (policy != null && policy.isEnabled() && policy.isOverrideFlag()) {
                IdentityApprovalRule rule = match(
                        rules.findByPolicyIdOrderByPositionAsc(policy.getId()),
                        amount, lineCount, maxLineAmount);
                if (rule == null) {
                    return ApprovalResolutionDto.none();
                }
                String profile = catalogProfile(rule.getProfile());
                if (profile == null) {
                    return ApprovalResolutionDto.none();
                }
                List<LevelRoleDto> levels = assignees.findByPolicyIdOrderByLevelAsc(policy.getId()).stream()
                        .map(a -> new LevelRoleDto(levelNumber(a.getLevel()), a.getAssigneeValue()))
                        .filter(l -> l.level() > 0 && l.role() != null && !l.role().isBlank())
                        .toList();
                return new ApprovalResolutionDto(true, profile, node.getId(), node.getCode(), rule.getCode(), levels);
            }
            current = node.getParentId();
        }
        return ApprovalResolutionDto.none();
    }

    private ApprovalPolicyDto toDto(IdentityApprovalPolicy policy) {
        String code = orgs.findById(policy.getOrgNodeId()).map(IdentityOrgNode::getCode).orElse(null);
        List<ApprovalRuleDto> ruleDtos = rules.findByPolicyIdOrderByPositionAsc(policy.getId()).stream()
                .map(ApprovalRuleDto::from)
                .toList();
        List<ApprovalAssigneeDto> assigneeDtos = assignees.findByPolicyIdOrderByLevelAsc(policy.getId()).stream()
                .map(ApprovalAssigneeDto::from)
                .toList();
        return ApprovalPolicyDto.from(policy, code, ruleDtos, assigneeDtos);
    }

    private IdentityApprovalRule toRule(UUID policyId, ApprovalRuleDto dto, int fallbackPosition) {
        if (dto == null || dto.code() == null || dto.code().isBlank() || dto.label() == null || dto.label().isBlank()) {
            throw new IllegalArgumentException("Chaque règle a un code et un libellé.");
        }
        String profile = dto.profile() == null ? "" : dto.profile().trim().toUpperCase(Locale.ROOT);
        if (!PROFILES.contains(profile)) {
            throw new IllegalArgumentException("Profil inconnu : " + dto.profile());
        }
        return IdentityApprovalRule.builder()
                .policyId(policyId)
                .code(dto.code().trim())
                .label(dto.label().trim())
                .position(dto.position() >= 0 ? dto.position() : fallbackPosition)
                .totalAmountLte(dto.elseRule() ? null : dto.totalAmountLte())
                .lineCountLte(dto.elseRule() ? null : dto.lineCountLte())
                .maxLineAmountLte(dto.elseRule() ? null : dto.maxLineAmountLte())
                .elseRule(dto.elseRule())
                .profile(profile)
                .build();
    }

    private IdentityApprovalAssignee toAssignee(UUID policyId, ApprovalAssigneeDto dto) {
        if (dto == null || dto.level() == null || dto.value() == null || dto.value().isBlank()) {
            throw new IllegalArgumentException("Chaque niveau a un rôle.");
        }
        String level = dto.level().trim().toUpperCase(Locale.ROOT);
        String scope = dto.scope() == null ? "ENTITY" : dto.scope().trim().toUpperCase(Locale.ROOT);
        if (!LEVELS.contains(level) || !SCOPES.contains(scope)) {
            throw new IllegalArgumentException("Niveau ou portée invalide.");
        }
        return IdentityApprovalAssignee.builder()
                .policyId(policyId)
                .level(level)
                .assigneeType(dto.type() == null || dto.type().isBlank() ? "ROLE" : dto.type().trim().toUpperCase(Locale.ROOT))
                .assigneeValue(dto.value().trim().toUpperCase(Locale.ROOT))
                .assigneeScope(scope)
                .build();
    }

    private static IdentityApprovalRule match(
            List<IdentityApprovalRule> rules,
            long amount,
            Integer lineCount,
            Long maxLineAmount
    ) {
        List<IdentityApprovalRule> ordered = new ArrayList<>(rules);
        ordered.sort(Comparator.comparing(IdentityApprovalRule::isElseRule));
        for (IdentityApprovalRule rule : ordered) {
            if (matches(rule, amount, lineCount, maxLineAmount)) {
                return rule;
            }
        }
        return null;
    }

    private static boolean matches(IdentityApprovalRule rule, long amount, Integer lineCount, Long maxLineAmount) {
        if (rule.isElseRule()) {
            return true;
        }
        if (rule.getTotalAmountLte() != null && amount > rule.getTotalAmountLte()) {
            return false;
        }
        if (rule.getLineCountLte() != null && (lineCount == null || lineCount > rule.getLineCountLte())) {
            return false;
        }
        if (rule.getMaxLineAmountLte() != null && (maxLineAmount == null || maxLineAmount > rule.getMaxLineAmountLte())) {
            return false;
        }
        return rule.getTotalAmountLte() != null
                || rule.getLineCountLte() != null
                || rule.getMaxLineAmountLte() != null;
    }

    /** Codes historiques du portail → profils exécutés par le moteur. */
    static String catalogProfile(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "NO_APPROVAL" -> "NO_APPROVAL";
            case "L1_CHECKER", "LEVEL_1" -> "LEVEL_1";
            case "L1_L2", "LEVEL_1_2" -> "LEVEL_1_2";
            case "HIGH_VALUE" -> "HIGH_VALUE";
            default -> null;
        };
    }

    private static int levelNumber(String level) {
        if (level == null) {
            return 0;
        }
        String value = level.trim().toUpperCase(Locale.ROOT);
        if (value.startsWith("L") && value.length() > 1) {
            try {
                return Integer.parseInt(value.substring(1));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private static String tenant(String tenantId) {
        return tenantId == null || tenantId.isBlank() ? IdentityService.DEFAULT_TENANT : tenantId.trim();
    }
}
