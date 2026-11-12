package com.gateway.payment.rules.api.dto;

import com.gateway.payment.rules.domain.*;
import com.gateway.payment.rules.moteur.DemandeEvaluation;
import com.gateway.payment.rules.moteur.ResultatEvaluation;
import com.gateway.payment.rules.moteur.ViolationDetail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RulesDtos {
    private RulesDtos() {}

    public record EvaluateRequest(
            String transactionId,
            String transactionType,
            String productCode,
            String groupCode,
            BigDecimal amount,
            String currency,
            String customerId,
            String walletId,
            String agentId,
            String merchantId,
            Map<String, Object> context
    ) {
        public DemandeEvaluation toDemande() {
            return new DemandeEvaluation(
                    transactionId, transactionType, productCode, groupCode,
                    amount, currency == null ? "XOF" : currency,
                    customerId, walletId, agentId, merchantId,
                    context == null ? Map.of() : context);
        }
    }

    public record ViolationDto(
            String ruleCode,
            int ruleVersion,
            String message,
            BigDecimal currentCumul,
            BigDecimal requestedAmount,
            BigDecimal projectedCumul,
            BigDecimal limit
    ) {
        static ViolationDto from(ViolationDetail v) {
            return new ViolationDto(
                    v.ruleCode(), v.ruleVersion(), v.message(),
                    v.currentValue(), v.requestedValue(), v.projectedValue(), v.limitValue());
        }
    }

    public record EvaluationResponse(
            String transactionId,
            String groupCode,
            String decision,
            boolean authorized,
            boolean simulate,
            UUID reservationId,
            List<ViolationDto> violations,
            List<String> evaluatedRules
    ) {
        public static EvaluationResponse from(ResultatEvaluation r) {
            return new EvaluationResponse(
                    r.transactionId(),
                    r.groupCode(),
                    r.decision().name(),
                    r.authorized(),
                    r.simulate(),
                    r.reservationId(),
                    r.violations().stream().map(ViolationDto::from).toList(),
                    r.evaluatedRules());
        }
    }

    public record RuleVersionDto(
            UUID id, int version, int priority, String conditionsJson,
            String sourceType, String sourcePeriod, String sourceScope,
            boolean includeCurrent, String operator, BigDecimal limitValue,
            String limitValuesJson, String actionOnViolation, String message,
            boolean mandatory, LocalDate effectiveFrom, LocalDate effectiveTo, String status
    ) {
        public static RuleVersionDto from(RuleVersion v) {
            return new RuleVersionDto(
                    v.getId(), v.getVersion(), v.getPriority(), v.getConditionsJson(),
                    v.getSourceType().name(),
                    v.getSourcePeriod() != null ? v.getSourcePeriod().name() : null,
                    v.getSourceScope() != null ? v.getSourceScope().name() : null,
                    v.isIncludeCurrent(),
                    v.getOperator().symbol(),
                    v.getLimitValue(), v.getLimitValuesJson(),
                    v.getActionOnViolation().name(), v.getMessage(),
                    v.isMandatory(), v.getEffectiveFrom(), v.getEffectiveTo(),
                    v.getStatus().name());
        }
    }

    public record RuleDto(
            UUID id, String code, String libelle, String description, String category, String status,
            String legalRef, List<RuleVersionDto> versions,
            BigDecimal limitValue, String sourceType, String sourcePeriod, String sourceScope,
            String operator, LocalDate effectiveFrom, String message, String conditionsJson,
            Integer priority, Boolean includeCurrent
    ) {
        public static RuleDto from(RuleDefinition r) {
            RuleVersion active = active(r);
            return new RuleDto(
                    r.getId(), r.getCode(), r.getLibelle(), r.getDescription(),
                    r.getCategory().name(), r.getStatus().name(), r.getLegalRef(),
                    r.getVersions().stream().map(RuleVersionDto::from).toList(),
                    active == null ? null : active.getLimitValue(),
                    active == null ? null : active.getSourceType().name(),
                    active == null || active.getSourcePeriod() == null ? null : active.getSourcePeriod().name(),
                    active == null || active.getSourceScope() == null ? null : active.getSourceScope().name(),
                    active == null ? null : active.getOperator().symbol(),
                    active == null ? null : active.getEffectiveFrom(),
                    active == null ? null : active.getMessage(),
                    active == null ? null : active.getConditionsJson(),
                    active == null ? null : active.getPriority(),
                    active == null ? null : active.isIncludeCurrent());
        }

        public static RuleDto summary(RuleDefinition r) {
            RuleVersion active = active(r);
            return new RuleDto(
                    r.getId(), r.getCode(), r.getLibelle(), r.getDescription(),
                    r.getCategory().name(), r.getStatus().name(), r.getLegalRef(),
                    List.of(),
                    active == null ? null : active.getLimitValue(),
                    active == null ? null : active.getSourceType().name(),
                    active == null || active.getSourcePeriod() == null ? null : active.getSourcePeriod().name(),
                    active == null || active.getSourceScope() == null ? null : active.getSourceScope().name(),
                    active == null ? null : active.getOperator().symbol(),
                    active == null ? null : active.getEffectiveFrom(),
                    active == null ? null : active.getMessage(),
                    active == null ? null : active.getConditionsJson(),
                    active == null ? null : active.getPriority(),
                    active == null ? null : active.isIncludeCurrent());
        }

        private static RuleVersion active(RuleDefinition r) {
            return r.getVersions().stream()
                    .filter(v -> v.getStatus() == RuleStatus.PUBLIEE)
                    .findFirst()
                    .orElse(r.getVersions().isEmpty() ? null : r.getVersions().getFirst());
        }
    }

    public record GroupItemDto(UUID ruleVersionId, String ruleCode, int ruleVersion, int sortOrder, String message) {
        public static GroupItemDto from(RuleGroupItem i) {
            RuleVersion v = i.getRuleVersion();
            return new GroupItemDto(
                    v.getId(), v.getRule().getCode(), v.getVersion(), i.getSortOrder(), v.getMessage());
        }
    }

    public record GroupDto(
            UUID id, String code, String libelle, String description,
            String transactionType, String productCode, String evaluationStrategy, String status,
            List<GroupItemDto> items
    ) {
        public static GroupDto from(RuleGroup g) {
            return new GroupDto(
                    g.getId(), g.getCode(), g.getLibelle(), g.getDescription(),
                    g.getTransactionType(), g.getProductCode(),
                    g.getEvaluationStrategy().name(), g.getStatus(),
                    g.getItems().stream().map(GroupItemDto::from).toList());
        }

        public static GroupDto summary(RuleGroup g) {
            return new GroupDto(
                    g.getId(), g.getCode(), g.getLibelle(), g.getDescription(),
                    g.getTransactionType(), g.getProductCode(),
                    g.getEvaluationStrategy().name(), g.getStatus(),
                    List.of());
        }
    }

    public record SaveGroupRequest(
            String code, String libelle, String description, String transactionType,
            String productCode, String evaluationStrategy, String status, List<String> ruleCodes
    ) {}

    public record CreateRuleRequest(
            String code, String libelle, String description, String category,
            Integer priority, String conditionsJson, String sourceType, String sourcePeriod,
            String sourceScope, Boolean includeCurrent, String operator, BigDecimal limitValue,
            String limitValuesJson, String actionOnViolation, String message, Boolean mandatory,
            String legalRef, LocalDate effectiveFrom, String groupCode, Boolean publish
    ) {}

    public record UpdateRuleRequest(
            String libelle, String description, String legalRef, String message,
            BigDecimal limitValue, String operator, String sourceType, String sourcePeriod,
            String sourceScope, Boolean includeCurrent, String conditionsJson, LocalDate effectiveFrom,
            LocalDate effectiveTo, Boolean mandatory, Integer priority
    ) {}
}
