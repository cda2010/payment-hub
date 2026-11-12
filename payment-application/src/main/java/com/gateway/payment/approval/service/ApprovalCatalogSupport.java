package com.gateway.payment.approval.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.approval.domain.ApprovalAssignmentType;
import com.gateway.payment.approval.domain.ApprovalProfileEntity;
import com.gateway.payment.approval.model.ApprovalAssignment;
import com.gateway.payment.approval.model.ApprovalLevel;
import com.gateway.payment.approval.model.ApprovalPolicy;
import com.gateway.payment.approval.model.ApprovalProfile;
import com.gateway.payment.approval.repository.ApprovalProfileRepository;
import com.gateway.payment.bpm.dmn.DmnTransferApprovalConfig;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class ApprovalCatalogSupport {

    private final ApprovalProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    public ApprovalCatalogSupport(ApprovalProfileRepository profileRepository, ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public ApprovalPolicy parsePolicy(Map<String, Object> catalogConfiguration) {
        if (catalogConfiguration == null) {
            return ApprovalPolicy.disabled();
        }
        Object raw = catalogConfiguration.get("approvalPolicy");
        if (!(raw instanceof Map<?, ?> map)) {
            return ApprovalPolicy.disabled();
        }
        boolean enabled = truthy(map.get("enabled"), true);
        String code = str(map.get("code"));
        String strategy = firstNonBlank(str(map.get("strategy")), "SEQUENTIAL");
        String dmnKey = firstNonBlank(
                str(map.get("dmnDecisionKey")),
                str(map.get("decisionKey")),
                DmnTransferApprovalConfig.DECISION_KEY);
        boolean initiatorCanApprove = truthy(map.get("initiatorCanApprove"), false)
                || truthy(map.get("selfApprovalAllowed"), false);
        boolean sameUserAcross = truthy(map.get("sameUserAcrossLevelsAllowed"), false);
        String formKey = firstNonBlank(str(map.get("formKey")), "account-transfer-approval");
        List<String> critical = new ArrayList<>();
        Object cf = map.get("criticalFields");
        if (cf instanceof List<?> list) {
            for (Object o : list) {
                if (o != null && StringUtils.hasText(String.valueOf(o))) {
                    critical.add(String.valueOf(o).trim());
                }
            }
        }
        if (critical.isEmpty()) {
            critical = List.of("amount", "currency", "destAccount", "sourceAccount");
        }
        return new ApprovalPolicy(
                code, enabled, strategy, dmnKey, initiatorCanApprove, sameUserAcross, formKey, critical);
    }

    public ApprovalProfile loadProfile(String profileCode) {
        String code = StringUtils.hasText(profileCode)
                ? profileCode.trim().toUpperCase(Locale.ROOT)
                : DmnTransferApprovalConfig.PROFILE_NO_APPROVAL;
        return profileRepository.findByCodeIgnoreCaseAndActifTrue(code)
                .map(this::toProfile)
                .orElseGet(() -> ApprovalProfile.none(code));
    }

    public ApprovalProfile toProfile(ApprovalProfileEntity entity) {
        try {
            Map<String, Object> def = objectMapper.readValue(
                    entity.getDefinitionJson(), new TypeReference<>() {});
            String strategy = firstNonBlank(str(def.get("strategy")), "SEQUENTIAL");
            List<ApprovalLevel> levels = parseLevels(def.get("levels"));
            return new ApprovalProfile(entity.getCode(), strategy, levels);
        } catch (Exception e) {
            return ApprovalProfile.none(entity.getCode());
        }
    }

    @SuppressWarnings("unchecked")
    private List<ApprovalLevel> parseLevels(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<ApprovalLevel> out = new ArrayList<>();
        int idx = 1;
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> m)) {
                continue;
            }
            int level = toInt(m.get("level"), idx);
            int required = toInt(m.get("requiredApprovals"), 1);
            List<ApprovalAssignment> assignments = parseAssignments(m.get("assignments"));
            out.add(new ApprovalLevel(level, required, assignments));
            idx++;
        }
        out.sort((a, b) -> Integer.compare(a.level(), b.level()));
        return out;
    }

    private List<ApprovalAssignment> parseAssignments(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<ApprovalAssignment> out = new ArrayList<>();
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> m)) {
                continue;
            }
            String typeStr = firstNonBlank(str(m.get("type")), "ROLE");
            String value = str(m.get("value"));
            if (!StringUtils.hasText(value)) {
                continue;
            }
            ApprovalAssignmentType type;
            try {
                type = ApprovalAssignmentType.valueOf(typeStr.toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                type = ApprovalAssignmentType.ROLE;
            }
            out.add(new ApprovalAssignment(type, value.trim()));
        }
        return out;
    }

    public String candidateGroupsCsv(ApprovalLevel level) {
        if (level == null || level.assignments() == null) {
            return "";
        }
        List<String> groups = new ArrayList<>();
        for (ApprovalAssignment a : level.assignments()) {
            if (a.type() == ApprovalAssignmentType.ROLE || a.type() == ApprovalAssignmentType.GROUP) {
                if (StringUtils.hasText(a.value())) {
                    groups.add(a.value().trim());
                }
            }
        }
        return String.join(",", groups);
    }

    public String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
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
