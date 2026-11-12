package com.gateway.payment.rules.service;

import com.gateway.payment.rules.api.dto.RulesDtos.*;
import com.gateway.payment.rules.domain.*;
import com.gateway.payment.rules.repository.LimitDefinitionRepository;
import com.gateway.payment.rules.repository.RuleDefinitionRepository;
import com.gateway.payment.rules.repository.RuleGroupRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CatalogueRulesService {

    private final RuleDefinitionRepository rules;
    private final RuleGroupRepository groups;
    private final LimitDefinitionRepository limits;

    @PersistenceContext
    private EntityManager entityManager;

    public CatalogueRulesService(
            RuleDefinitionRepository rules,
            RuleGroupRepository groups,
            LimitDefinitionRepository limits
    ) {
        this.rules = rules;
        this.groups = groups;
        this.limits = limits;
    }

    @Transactional(readOnly = true)
    public List<RuleDto> listerRegles() {
        return rules.findAllByOrderByCodeAsc().stream().map(RuleDto::summary).toList();
    }

    @Transactional(readOnly = true)
    public RuleDto detailRegle(UUID id) {
        RuleDefinition r = rules.findDetailedById(id)
                .orElseThrow(() -> new IllegalArgumentException("Règle introuvable"));
        return RuleDto.from(r);
    }

    @Transactional
    public RuleDto creerBrouillon(CreateRuleRequest req) {
        if (req.code() == null || req.code().isBlank()) {
            throw new IllegalArgumentException("code requis");
        }
        if (rules.findByCode(req.code()).isPresent()) {
            throw new IllegalArgumentException("Code déjà utilisé: " + req.code());
        }
        RuleDefinition def = RuleDefinition.builder()
                .code(req.code().trim())
                .libelle(req.libelle())
                .description(req.description())
                .category(RuleCategory.valueOf(req.category()))
                .status(RuleStatus.BROUILLON)
                .legalRef(req.legalRef())
                .build();
        boolean publish = Boolean.TRUE.equals(req.publish());
        LocalDate effectiveFrom = req.effectiveFrom() == null ? LocalDate.now() : req.effectiveFrom();
        RuleVersion v = RuleVersion.builder()
                .rule(def)
                .version(1)
                .priority(req.priority() == null ? 100 : req.priority())
                .conditionsJson(req.conditionsJson() == null ? "{}" : req.conditionsJson())
                .sourceType(SourceType.valueOf(req.sourceType()))
                .sourcePeriod(blank(req.sourcePeriod()) ? null : LimitPeriod.valueOf(req.sourcePeriod()))
                .sourceScope(blank(req.sourceScope()) ? null : LimitScope.valueOf(req.sourceScope()))
                .includeCurrent(req.includeCurrent() == null || req.includeCurrent())
                .operator(RuleOperator.fromSymbol(req.operator()))
                .limitValue(req.limitValue())
                .limitValuesJson(req.limitValuesJson())
                .actionOnViolation(req.actionOnViolation() == null
                        ? RuleDecision.REJECT
                        : RuleDecision.valueOf(req.actionOnViolation()))
                .message(req.message() == null ? "Règle violée" : req.message())
                .mandatory(req.mandatory() == null || req.mandatory())
                .effectiveFrom(effectiveFrom)
                .status(publish ? RuleStatus.PUBLIEE : RuleStatus.BROUILLON)
                .publishedAt(publish ? Instant.now() : null)
                .build();
        if (publish) {
            def.setStatus(RuleStatus.PUBLIEE);
        }
        def.setLegalEffective(effectiveFrom);
        def.getVersions().add(v);
        RuleDefinition saved = rules.save(def);
        attach(v, req.groupCode(), v.getPriority());
        syncLimit(saved.getCode(), saved.getLibelle(), v);
        return RuleDto.from(saved);
    }

    @Transactional
    public RuleDto modifier(UUID id, UpdateRuleRequest req) {
        RuleDefinition def = rules.findDetailedById(id)
                .orElseThrow(() -> new IllegalArgumentException("Règle introuvable"));
        if (req.libelle() != null && !req.libelle().isBlank()) {
            def.setLibelle(req.libelle().trim());
        }
        if (req.description() != null) {
            def.setDescription(req.description());
        }
        if (req.legalRef() != null) {
            def.setLegalRef(req.legalRef());
        }
        RuleVersion version = def.getVersions().stream()
                .filter(v -> v.getStatus() == RuleStatus.PUBLIEE)
                .findFirst()
                .orElseGet(() -> def.getVersions().stream().findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Aucune version à modifier")));
        if (req.message() != null && !req.message().isBlank()) {
            version.setMessage(req.message().trim());
        }
        if (req.limitValue() != null) {
            version.setLimitValue(req.limitValue());
        }
        if (req.operator() != null && !req.operator().isBlank()) {
            version.setOperator(RuleOperator.fromSymbol(req.operator()));
        }
        if (req.sourceType() != null && !req.sourceType().isBlank()) {
            version.setSourceType(SourceType.valueOf(req.sourceType()));
            version.setSourcePeriod(blank(req.sourcePeriod()) ? null : LimitPeriod.valueOf(req.sourcePeriod()));
            version.setSourceScope(blank(req.sourceScope()) ? null : LimitScope.valueOf(req.sourceScope()));
        }
        if (req.includeCurrent() != null) {
            version.setIncludeCurrent(req.includeCurrent());
        }
        if (req.conditionsJson() != null && !req.conditionsJson().isBlank()) {
            version.setConditionsJson(req.conditionsJson());
        }
        if (req.effectiveFrom() != null) {
            version.setEffectiveFrom(req.effectiveFrom());
            def.setLegalEffective(req.effectiveFrom());
        }
        if (req.effectiveTo() != null) {
            version.setEffectiveTo(req.effectiveTo());
        }
        if (req.mandatory() != null) {
            version.setMandatory(req.mandatory());
        }
        if (req.priority() != null) {
            version.setPriority(req.priority());
        }
        syncLimit(def.getCode(), def.getLibelle(), version);
        return RuleDto.from(rules.save(def));
    }

    private void attach(RuleVersion version, String groupCode, int sortOrder) {
        if (blank(groupCode)) {
            return;
        }
        RuleGroup group = groups.findByCode(groupCode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable: " + groupCode));
        boolean already = group.getItems().stream()
                .anyMatch(item -> item.getRuleVersion().getId().equals(version.getId()));
        if (already) {
            return;
        }
        RuleGroupItem item = RuleGroupItem.builder()
                .group(group)
                .ruleVersion(version)
                .sortOrder(sortOrder)
                .build();
        group.getItems().add(item);
        groups.save(group);
    }

    private void syncLimit(String code, String libelle, RuleVersion version) {
        if (version.getLimitValue() == null) {
            return;
        }
        LimitDefinition limit = limits.findByCode(code).orElseGet(() -> LimitDefinition.builder()
                .code(code)
                .currency("XOF")
                .metric(metricOf(version))
                .active(true)
                .build());
        limit.setMetric(metricOf(version));
        limit.setLibelle(libelle);
        limit.setLimitValue(version.getLimitValue());
        limit.setPeriod(version.getSourcePeriod() == null ? LimitPeriod.CUSTOM : version.getSourcePeriod());
        limit.setScope(version.getSourceScope() == null ? LimitScope.CUSTOMER : version.getSourceScope());
        limit.setActive(version.getStatus() != RuleStatus.DESACTIVEE);
        limits.save(limit);
    }

    private static String metricOf(RuleVersion version) {
        SourceType type = version.getSourceType();
        if (type == SourceType.CUMUL_COUNT || type == SourceType.TRANSACTION_COUNT) {
            return "COUNT";
        }
        return "AMOUNT";
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    @Transactional
    public RuleDto publier(UUID id) {
        RuleDefinition def = rules.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Règle introuvable"));
        RuleVersion draft = def.getVersions().stream()
                .filter(v -> v.getStatus() == RuleStatus.BROUILLON || v.getStatus() == RuleStatus.VALIDEE)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Aucune version à publier"));
        draft.setStatus(RuleStatus.PUBLIEE);
        draft.setPublishedAt(Instant.now());
        def.setStatus(RuleStatus.PUBLIEE);
        return RuleDto.from(rules.save(def));
    }

    @Transactional(readOnly = true)
    public List<GroupDto> listerGroupes() {
        return groups.findAllByOrderByCodeAsc().stream().map(GroupDto::summary).toList();
    }

    @Transactional
    public GroupDto creerGroupe(SaveGroupRequest req) {
        if (blank(req.code())) {
            throw new IllegalArgumentException("code requis");
        }
        if (blank(req.libelle())) {
            throw new IllegalArgumentException("libellé requis");
        }
        String code = req.code().trim().toUpperCase(Locale.ROOT);
        if (groups.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Code déjà utilisé: " + code);
        }
        RuleGroup group = RuleGroup.builder()
                .code(code)
                .libelle(req.libelle().trim())
                .description(blankToNull(req.description()))
                .transactionType(upperOrNull(req.transactionType()))
                .productCode(upperOrNull(req.productCode()))
                .evaluationStrategy(strategy(req.evaluationStrategy()))
                .status(status(req.status()))
                .build();
        groups.save(group);
        replaceItems(group, req.ruleCodes());
        return GroupDto.from(groups.save(group));
    }

    @Transactional
    public GroupDto modifierGroupe(String code, SaveGroupRequest req) {
        RuleGroup group = groups.findDetailedByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable: " + code));
        if (!blank(req.libelle())) {
            group.setLibelle(req.libelle().trim());
        }
        if (req.description() != null) {
            group.setDescription(blankToNull(req.description()));
        }
        if (req.transactionType() != null) {
            group.setTransactionType(upperOrNull(req.transactionType()));
        }
        if (req.productCode() != null) {
            group.setProductCode(upperOrNull(req.productCode()));
        }
        if (!blank(req.evaluationStrategy())) {
            group.setEvaluationStrategy(strategy(req.evaluationStrategy()));
        }
        if (!blank(req.status())) {
            group.setStatus(status(req.status()));
        }
        replaceItems(group, req.ruleCodes());
        return GroupDto.from(groups.save(group));
    }

    private void replaceItems(RuleGroup group, List<String> ruleCodes) {
        if (ruleCodes == null) {
            return;
        }
        group.getItems().clear();
        entityManager.flush();
        int order = 10;
        for (String code : new LinkedHashSet<>(ruleCodes)) {
            if (blank(code)) {
                continue;
            }
            RuleDefinition definition = rules.findByCode(code.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Règle introuvable: " + code));
            RuleVersion version = definition.getVersions().stream()
                    .filter(v -> v.getStatus() == RuleStatus.PUBLIEE)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Aucune version publiée pour " + definition.getCode()));
            group.getItems().add(RuleGroupItem.builder()
                    .group(group)
                    .ruleVersion(version)
                    .sortOrder(order)
                    .build());
            order += 10;
        }
    }

    private static EvaluationStrategy strategy(String raw) {
        if (blank(raw)) {
            return EvaluationStrategy.STOP_ON_REJECT;
        }
        return EvaluationStrategy.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    private static String status(String raw) {
        if (blank(raw) || "ACTIVE".equalsIgnoreCase(raw.trim())) {
            return "ACTIVE";
        }
        if ("INACTIVE".equalsIgnoreCase(raw.trim())) {
            return "INACTIVE";
        }
        throw new IllegalArgumentException("Statut de groupe inconnu: " + raw);
    }

    private static String upperOrNull(String value) {
        return blank(value) ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        return blank(value) ? null : value.trim();
    }

    @Transactional(readOnly = true)
    public GroupDto detailGroupe(String code) {
        return groups.findDetailedByCode(code)
                .map(GroupDto::from)
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable: " + code));
    }
}
