package com.gateway.payment.rules.moteur;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.rules.domain.*;
import com.gateway.payment.rules.repository.RuleExecutionRepository;
import com.gateway.payment.rules.repository.RuleGroupRepository;
import com.gateway.payment.rules.service.CumulService;
import com.gateway.payment.rules.source.CumulSnapshot;
import com.gateway.payment.rules.source.SourceRegistry;
import com.gateway.payment.rules.source.SourceValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Moteur d'évaluation V1 — interne (pas Drools).
 * soft-rules catalogue restent dans payment-soft-rules.
 */
@Service
public class MoteurRegles {

    private final RuleGroupRepository groups;
    private final SourceRegistry sources;
    private final CumulService cumuls;
    private final RuleExecutionRepository executions;
    private final ObjectMapper mapper;

    public MoteurRegles(
            RuleGroupRepository groups,
            SourceRegistry sources,
            CumulService cumuls,
            RuleExecutionRepository executions,
            ObjectMapper mapper
    ) {
        this.groups = groups;
        this.sources = sources;
        this.cumuls = cumuls;
        this.executions = executions;
        this.mapper = mapper;
    }

    @Transactional
    public ResultatEvaluation simulate(DemandeEvaluation demande) {
        return evaluate(demande, true);
    }

    @Transactional
    public ResultatEvaluation evaluate(DemandeEvaluation demande) {
        return evaluate(demande, false);
    }

    private ResultatEvaluation evaluate(DemandeEvaluation demande, boolean simulate) {
        if (demande.transactionId() == null || demande.transactionId().isBlank()) {
            throw new IllegalArgumentException("transactionId requis");
        }
        if (demande.transactionType() == null || demande.transactionType().isBlank()) {
            throw new IllegalArgumentException("transactionType requis");
        }
        if (demande.amount() == null || demande.amount().signum() < 0) {
            throw new IllegalArgumentException("amount >= 0 requis");
        }

        RuleGroup group = resolveGroup(demande);
        List<RuleVersion> rules = group.getItems().stream()
                .sorted(Comparator.comparingInt(RuleGroupItem::getSortOrder)
                        .thenComparingInt(i -> i.getRuleVersion().getPriority()))
                .map(RuleGroupItem::getRuleVersion)
                .filter(v -> v.getStatus() == RuleStatus.PUBLIEE)
                .filter(v -> v.isEffectiveOn(LocalDate.now()))
                .filter(v -> matchesConditions(v, demande))
                .toList();

        List<ViolationDetail> violations = new ArrayList<>();
        List<String> evaluated = new ArrayList<>();
        RuleDecision decision = RuleDecision.ALLOW;
        List<CumulService.PendingReservation> pending = new ArrayList<>();

        EvaluationStrategy strategy = group.getEvaluationStrategy() == null
                ? EvaluationStrategy.STOP_ON_REJECT
                : group.getEvaluationStrategy();

        for (RuleVersion rule : rules) {
            evaluated.add(rule.getRule().getCode() + "@v" + rule.getVersion());
            CumulSnapshot snap = null;
            if (rule.getSourceType() == SourceType.CUMUL_AMOUNT || rule.getSourceType() == SourceType.CUMUL_COUNT) {
                snap = cumuls.snapshot(rule.getRule().getCode(), rule.getSourceScope(), rule.getSourcePeriod(), demande);
            }
            SourceValue value = sources.resolve(rule, demande, snap);
            Object actual = value.cumulSource() ? value.projected() : value.raw();

            boolean ok = OperatorEval.satisfied(
                    rule.getOperator(), actual, rule.getLimitValue(), rule.getLimitValuesJson(), mapper);

            if (!ok) {
                ViolationDetail v = new ViolationDetail(
                        rule.getRule().getCode(),
                        rule.getVersion(),
                        rule.getActionOnViolation(),
                        rule.getMessage(),
                        value.currentCumul() != null ? value.currentCumul() : value.numeric(),
                        demande.amount(),
                        value.projected() != null ? value.projected() : value.numeric(),
                        rule.getLimitValue());
                violations.add(v);
                decision = RuleDecision.merge(decision, rule.getActionOnViolation());
                if (strategy == EvaluationStrategy.STOP_ON_REJECT && rule.getActionOnViolation().blocks()) {
                    break;
                }
            } else if (!simulate && (rule.getSourceType() == SourceType.CUMUL_AMOUNT
                    || rule.getSourceType() == SourceType.CUMUL_COUNT)) {
                pending.add(new CumulService.PendingReservation(
                        rule.getRule().getCode(),
                        rule.getSourceScope(),
                        rule.getSourcePeriod(),
                        demande.amount(),
                        rule.getSourceType() == SourceType.CUMUL_AMOUNT));
            }
        }

        UUID reservationId = null;
        if (!simulate && decision == RuleDecision.ALLOW && !pending.isEmpty()) {
            for (CumulService.PendingReservation p : pending) {
                BigDecimal lim = rules.stream()
                        .filter(r -> r.getRule().getCode().equals(p.limitCode()))
                        .map(RuleVersion::getLimitValue)
                        .findFirst()
                        .orElse(null);
                UUID id = cumuls.tryReserve(
                        p.limitCode(), p.scope(), p.period(), demande, p.amount(), lim, p.amountMetric());
                if (id == null) {
                    decision = RuleDecision.REJECT;
                    violations.add(new ViolationDetail(
                            p.limitCode(), 0, RuleDecision.REJECT,
                            "Capacité déjà réservée / plafond concurrent",
                            null, demande.amount(), null, lim));
                    reservationId = null;
                    break;
                }
                reservationId = id;
            }
        }

        boolean authorized = decision == RuleDecision.ALLOW;
        persistExecution(demande, group.getCode(), decision, authorized, simulate, reservationId, violations);

        return new ResultatEvaluation(
                demande.transactionId(),
                group.getCode(),
                decision,
                authorized,
                simulate,
                reservationId,
                List.copyOf(violations),
                List.copyOf(evaluated));
    }

    private RuleGroup resolveGroup(DemandeEvaluation demande) {
        if (demande.groupCode() != null && !demande.groupCode().isBlank()) {
            return groups.findDetailedByCode(demande.groupCode())
                    .orElseThrow(() -> new IllegalArgumentException("Groupe inconnu: " + demande.groupCode()));
        }
        List<RuleGroup> candidates = groups.findActiveForTransactionType(demande.transactionType());
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("Aucun groupe de règles pour " + demande.transactionType());
        }
        // Préférence : code se terminant par _STANDARD, sinon premier
        return candidates.stream()
                .filter(g -> g.getCode().endsWith("_STANDARD"))
                .findFirst()
                .orElse(candidates.getFirst());
    }

    private boolean matchesConditions(RuleVersion version, DemandeEvaluation d) {
        Map<String, Object> cond = parseConditions(version.getConditionsJson());
        if (cond.isEmpty()) {
            return true;
        }
        if (cond.get("transactionTypes") instanceof List<?> types && !types.isEmpty()) {
            boolean listed = d.transactionType() != null && types.stream()
                    .anyMatch(type -> d.transactionType().equalsIgnoreCase(String.valueOf(type)));
            if (!listed) {
                return false;
            }
        } else if (cond.containsKey("transactionType") || cond.containsKey("typeOperation")) {
            Object expected = cond.getOrDefault("transactionType", cond.get("typeOperation"));
            if (expected != null && !String.valueOf(expected).equalsIgnoreCase(d.transactionType())) {
                return false;
            }
        }
        if (isTrue(cond.get("skipSameCustomer"))) {
            String sourceCustomer = first(d.ctx("sourceCustomerId"), d.ctx("payerCustomerId"));
            if (isTrue(d.ctx("sameCustomer"))
                    || (sourceCustomer != null && d.customerId() != null
                    && sourceCustomer.equalsIgnoreCase(d.customerId()))) {
                return false;
            }
        }
        if (cond.containsKey("kycLevel") || cond.containsKey("niveauKyc")) {
            Object expected = cond.getOrDefault("kycLevel", cond.get("niveauKyc"));
            String actual = first(d.ctx("kycLevel"), d.ctx("niveauKyc"));
            if (expected != null && (actual == null || !String.valueOf(expected).equalsIgnoreCase(actual))) {
                return false;
            }
        }
        if (cond.get("kycLevels") instanceof List<?> levels && !levels.isEmpty()) {
            String actual = first(d.ctx("kycLevel"), d.ctx("niveauKyc"));
            boolean identified = actual != null && levels.stream()
                    .anyMatch(level -> actual.equalsIgnoreCase(String.valueOf(level)));
            if (!identified) {
                return false;
            }
        }
        if (cond.get("excludeSubjectRoles") instanceof List<?> excluded && !excluded.isEmpty()) {
            String role = first(d.ctx("subjectRole"), first(d.ctx("participantKind"), d.ctx("role")));
            if (role != null && excluded.stream().anyMatch(item -> role.equalsIgnoreCase(String.valueOf(item)))) {
                return false;
            }
        }
        if (cond.containsKey("channel")) {
            String actual = d.ctx("channel");
            if (actual == null || !String.valueOf(cond.get("channel")).equalsIgnoreCase(actual)) {
                return false;
            }
        }
        return true;
    }

    private Map<String, Object> parseConditions(String json) {
        if (json == null || json.isBlank() || "{}".equals(json.trim())) {
            return Map.of();
        }
        try {
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private void persistExecution(
            DemandeEvaluation demande,
            String groupCode,
            RuleDecision decision,
            boolean authorized,
            boolean simulate,
            UUID reservationId,
            List<ViolationDetail> violations
    ) {
        RuleExecution exec = RuleExecution.builder()
                .transactionId(demande.transactionId())
                .groupCode(groupCode)
                .decision(decision)
                .authorized(authorized)
                .simulate(simulate)
                .reservationId(reservationId)
                .build();
        try {
            exec.setRequestJson(mapper.writeValueAsString(demande));
        } catch (Exception ignored) {
            exec.setRequestJson(null);
        }
        for (ViolationDetail v : violations) {
            RuleViolation rv = RuleViolation.builder()
                    .execution(exec)
                    .ruleCode(v.ruleCode())
                    .ruleVersion(v.ruleVersion())
                    .action(v.action())
                    .message(v.message())
                    .currentValue(v.currentValue())
                    .requestedValue(v.requestedValue())
                    .projectedValue(v.projectedValue())
                    .limitValue(v.limitValue())
                    .build();
            exec.getViolations().add(rv);
        }
        executions.save(exec);
    }

    private static String first(String a, String b) {
        return a != null && !a.isBlank() ? a : b;
    }

    private static boolean isTrue(Object value) {
        if (value instanceof Boolean flag) {
            return flag;
        }
        return value != null && "true".equalsIgnoreCase(String.valueOf(value));
    }
}
