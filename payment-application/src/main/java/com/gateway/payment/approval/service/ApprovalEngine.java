package com.gateway.payment.approval.service;

import com.gateway.payment.approval.domain.ApprovalDecision;
import com.gateway.payment.approval.domain.ApprovalDecisionOutcome;
import com.gateway.payment.approval.domain.ApprovalInstance;
import com.gateway.payment.approval.domain.ApprovalInstanceStatus;
import com.gateway.payment.approval.domain.ApprovalStepInstance;
import com.gateway.payment.approval.domain.ApprovalStepStatus;
import com.gateway.payment.approval.model.ApprovalDecideResult;
import com.gateway.payment.approval.model.ApprovalLevel;
import com.gateway.payment.approval.model.ApprovalPolicy;
import com.gateway.payment.approval.model.ApprovalProfile;
import com.gateway.payment.approval.repository.ApprovalDecisionRepository;
import com.gateway.payment.approval.repository.ApprovalInstanceRepository;
import com.gateway.payment.approval.repository.ApprovalStepInstanceRepository;
import com.gateway.payment.bpm.dmn.DmnTransferApprovalConfig;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.rapidframe.bpm.dmn.DmnEvaluateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Orchestre : Policy → DMN (profileCode) → Profile → Instance/Steps → Decisions.
 * Flowable reste responsable des userTasks ; cet engine porte les règles métier.
 */
@Service
public class ApprovalEngine {

    private static final Logger log = LoggerFactory.getLogger(ApprovalEngine.class);

    private final ApprovalCatalogSupport catalogSupport;
    private final ApprovalInstanceRepository instanceRepository;
    private final ApprovalStepInstanceRepository stepRepository;
    private final ApprovalDecisionRepository decisionRepository;
    private final DmnEvaluateService dmnEvaluateService;
    private final ApprovalIdentitySupport identitySupport;
    private final ObjectProvider<com.gateway.payment.inbox.PartyActionProducer> partyActionProducer;

    public ApprovalEngine(
            ApprovalCatalogSupport catalogSupport,
            ApprovalInstanceRepository instanceRepository,
            ApprovalStepInstanceRepository stepRepository,
            ApprovalDecisionRepository decisionRepository,
            ObjectProvider<DmnEvaluateService> dmnEvaluateService,
            ApprovalIdentitySupport identitySupport,
            ObjectProvider<com.gateway.payment.inbox.PartyActionProducer> partyActionProducer
    ) {
        this.catalogSupport = catalogSupport;
        this.instanceRepository = instanceRepository;
        this.stepRepository = stepRepository;
        this.decisionRepository = decisionRepository;
        this.dmnEvaluateService = dmnEvaluateService.getIfAvailable();
        this.identitySupport = identitySupport;
        this.partyActionProducer = partyActionProducer;
    }

    /**
     * Résout le profil et crée l'instance runtime. Pose les variables BPMN (groupes L1–L3).
     */
    @Transactional
    public ResolvedApproval start(
            PaymentTransaction tx,
            Map<String, Object> context,
            Map<String, Object> processVarsOut
    ) {
        @SuppressWarnings("unchecked")
        Map<String, Object> catalogCfg = context.get("catalogConfiguration") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : Map.of();
        ApprovalPolicy policy = catalogSupport.parsePolicy(catalogCfg);

        if (!policy.enabled()) {
            applyNoApproval(processVarsOut, context);
            return new ResolvedApproval(policy, ApprovalProfile.none(DmnTransferApprovalConfig.PROFILE_NO_APPROVAL), null);
        }

        String profileCode = resolveProfileCode(tx, context, policy);
        ApprovalProfile profile = catalogSupport.loadProfile(profileCode);
        String fingerprint = computeFingerprint(context, policy.criticalFields(), tx);
        String initiatedBy = str(context.get(DmnTransferApprovalConfig.CTX_INITIATED_BY));

        instanceRepository.findByPaymentId(tx.getPaymentId()).ifPresent(existing -> {
            existing.setStatus(ApprovalInstanceStatus.CANCELLED);
            existing.setCompletedAt(Instant.now());
            instanceRepository.save(existing);
        });

        ApprovalInstance instance = ApprovalInstance.builder()
                .paymentId(tx.getPaymentId())
                .policyCode(policy.code())
                .profileCode(profile.code())
                .strategy(profile.strategy())
                .status(profile.requiresApproval()
                        ? ApprovalInstanceStatus.IN_PROGRESS
                        : ApprovalInstanceStatus.APPROVED)
                .currentLevel(profile.requiresApproval() ? profile.levels().get(0).level() : null)
                .totalLevels(profile.levelCount())
                .policySnapshotJson(catalogSupport.writeJson(policy))
                .profileSnapshotJson(catalogSupport.writeJson(profile))
                .contextFingerprint(fingerprint)
                .initiatedBy(initiatedBy)
                .dmnDecisionKey(policy.dmnDecisionKey())
                .completedAt(profile.requiresApproval() ? null : Instant.now())
                .build();
        instance = instanceRepository.save(instance);

        int i = 0;
        for (ApprovalLevel level : profile.levels()) {
            i++;
            String groups = catalogSupport.candidateGroupsCsv(level);
            String taskKey = "APPROVAL_L" + level.level();
            ApprovalStepStatus st = (i == 1) ? ApprovalStepStatus.WAITING : ApprovalStepStatus.PENDING;
            ApprovalStepInstance step = ApprovalStepInstance.builder()
                    .approvalInstance(instance)
                    .levelNumber(level.level())
                    .requiredApprovals(level.requiredApprovals())
                    .collectedApprovals(0)
                    .status(st)
                    .assignmentsJson(catalogSupport.writeJson(level.assignments()))
                    .candidateGroups(groups)
                    .bpmTaskDefinitionKey(taskKey)
                    .build();
            stepRepository.save(step);
        }

        applyProfileVars(processVarsOut, context, profile, instance);
        context.put(DmnTransferApprovalConfig.VAR_APPROVAL_INSTANCE_ID, instance.getId().toString());
        context.put(DmnTransferApprovalConfig.VAR_PROFILE_CODE, profile.code());
        if (profile.requiresApproval()) {
            context.put("waitMessage", "Transfert en attente d'approbation (" + profile.code() + ")");
            UUID partyId = com.gateway.payment.inbox.PartyActionProducer.partyIdFromContext(context);
            com.gateway.payment.inbox.PartyActionProducer producer = partyActionProducer.getIfAvailable();
            if (producer != null && partyId != null) {
                producer.approvalPending(partyId, tx.getTenantId(), tx.getPaymentId(), profile.code());
            }
        }

        log.info("Approval started paymentId={} profile={} levels={}",
                tx.getPaymentId(), profile.code(), profile.levelCount());
        return new ResolvedApproval(policy, profile, instance);
    }

    @Transactional
    public ApprovalDecideResult decide(
            PaymentTransaction tx,
            Map<String, Object> context,
            boolean approved,
            String comment,
            String approverId
    ) {
        if (!StringUtils.hasText(approverId)) {
            throw new BusinessException("APPROVER_REQUIRED", "Validateur requis", HttpStatus.FORBIDDEN);
        }
        ApprovalInstance instance = instanceRepository.findByPaymentId(tx.getPaymentId())
                .orElseThrow(() -> new BusinessException("APPROVAL_NOT_FOUND",
                        "Aucune instance d'approbation pour ce paiement"));

        if (instance.getStatus() != ApprovalInstanceStatus.IN_PROGRESS
                && instance.getStatus() != ApprovalInstanceStatus.PENDING) {
            throw new BusinessException("APPROVAL_ALREADY_CLOSED", "Approbation déjà clôturée");
        }

        ApprovalPolicy policy = catalogSupport.parsePolicy(
                context.get("catalogConfiguration") instanceof Map<?, ?> m
                        ? castMap(m) : Map.of());

        assertMakerChecker(policy, instance, approverId);
        assertSameUserAcrossLevels(policy, instance, approverId);
        assertFingerprint(policy, instance, context, tx);

        ApprovalStepInstance step = stepRepository
                .findFirstByApprovalInstanceIdAndStatusOrderByLevelNumberAsc(
                        instance.getId(), ApprovalStepStatus.WAITING)
                .orElseThrow(() -> new BusinessException("NO_WAITING_STEP",
                        "Aucun niveau d'approbation en attente"));

        if (!identitySupport.matchesCandidateGroups(step.getCandidateGroups())) {
            throw new BusinessException(
                    "APPROVAL_NOT_AUTHORIZED",
                    "Vous n'appartenez pas au groupe de validation de cette étape",
                    HttpStatus.FORBIDDEN);
        }

        if (!approved) {
            ApprovalDecision decision = ApprovalDecision.builder()
                    .stepInstance(step)
                    .approverId(approverId.trim())
                    .approverRole(primaryRole(step))
                    .outcome(ApprovalDecisionOutcome.REJECT)
                    .comment(comment)
                    .build();
            decisionRepository.save(decision);
            step.setStatus(ApprovalStepStatus.REJECTED);
            step.setCompletedAt(Instant.now());
            stepRepository.save(step);
            instance.setStatus(ApprovalInstanceStatus.REJECTED);
            instance.setCompletedAt(Instant.now());
            instanceRepository.save(instance);
            closeApprovalInbox(context, tx.getPaymentId());
            return ApprovalDecideResult.rejected(
                    step.getLevelNumber(), instance.getTotalLevels(), "Approbation refusée");
        }

        // Déjà voté sur ce step ?
        List<ApprovalDecision> prior = decisionRepository.findByStepInstanceIdOrderByDecidedAtAsc(step.getId());
        for (ApprovalDecision d : prior) {
            if (approverId.equalsIgnoreCase(d.getApproverId())) {
                throw new BusinessException("ALREADY_DECIDED",
                        "Vous avez déjà tranché sur ce niveau");
            }
        }

        ApprovalDecision decision = ApprovalDecision.builder()
                .stepInstance(step)
                .approverId(approverId.trim())
                .approverRole(primaryRole(step))
                .outcome(ApprovalDecisionOutcome.APPROVE)
                .comment(comment)
                .build();
        decisionRepository.save(decision);

        step.setCollectedApprovals(step.getCollectedApprovals() + 1);
        if (step.getCollectedApprovals() < step.getRequiredApprovals()) {
            stepRepository.save(step);
            return ApprovalDecideResult.partial(
                    step.getLevelNumber(),
                    instance.getTotalLevels(),
                    "Validation enregistrée (" + step.getCollectedApprovals()
                            + "/" + step.getRequiredApprovals() + ")");
        }

        step.setStatus(ApprovalStepStatus.APPROVED);
        step.setCompletedAt(Instant.now());
        stepRepository.save(step);

        Optional<ApprovalStepInstance> next = stepRepository
                .findByApprovalInstanceIdAndLevelNumber(instance.getId(), step.getLevelNumber() + 1);
        // levels may not be sequential numbers 1,2,3 — find next PENDING by order
        if (next.isEmpty()) {
            List<ApprovalStepInstance> all = stepRepository
                    .findByApprovalInstanceIdOrderByLevelNumberAsc(instance.getId());
            next = all.stream()
                    .filter(s -> s.getStatus() == ApprovalStepStatus.PENDING)
                    .findFirst();
        } else if (next.get().getStatus() != ApprovalStepStatus.PENDING) {
            List<ApprovalStepInstance> all = stepRepository
                    .findByApprovalInstanceIdOrderByLevelNumberAsc(instance.getId());
            next = all.stream()
                    .filter(s -> s.getStatus() == ApprovalStepStatus.PENDING)
                    .findFirst();
        }

        if (next.isPresent()) {
            ApprovalStepInstance n = next.get();
            n.setStatus(ApprovalStepStatus.WAITING);
            stepRepository.save(n);
            instance.setCurrentLevel(n.getLevelNumber());
            instanceRepository.save(instance);
            return ApprovalDecideResult.levelComplete(
                    step.getLevelNumber(), instance.getTotalLevels(), false,
                    "Niveau " + step.getLevelNumber() + " approuvé — passage niveau " + n.getLevelNumber());
        }

        instance.setStatus(ApprovalInstanceStatus.APPROVED);
        instance.setCompletedAt(Instant.now());
        instance.setCurrentLevel(null);
        instanceRepository.save(instance);
        closeApprovalInbox(context, tx.getPaymentId());
        return ApprovalDecideResult.levelComplete(
                step.getLevelNumber(), instance.getTotalLevels(), true,
                "Circuit d'approbation terminé");
    }

    private void closeApprovalInbox(Map<String, Object> context, String paymentId) {
        com.gateway.payment.inbox.PartyActionProducer producer = partyActionProducer.getIfAvailable();
        if (producer == null) {
            return;
        }
        UUID partyId = com.gateway.payment.inbox.PartyActionProducer.partyIdFromContext(context);
        if (partyId != null) {
            producer.approvalClosed(partyId, paymentId);
        }
    }

    public Optional<ApprovalInstance> findByPaymentId(String paymentId) {
        return instanceRepository.findByPaymentId(paymentId);
    }

    private String resolveProfileCode(PaymentTransaction tx, Map<String, Object> context, ApprovalPolicy policy) {
        // Facture : le montant utile est souvent amountDue tant que tx.amount n'est pas encore posé.
        BigDecimal amount = firstPositive(
                tx.getAmount(),
                toDecimal(context.get("amount")),
                toDecimal(context.get("amountDue")));
        String currency = firstNonBlank(str(context.get("currency")), tx.getCurrency(), "XOF");
        String catalogServiceCode = firstNonBlank(
                str(context.get("catalogServiceCode")), "ACCOUNT_TRANSFER");

        Map<String, Object> vars = new HashMap<>();
        vars.put("amount", amount != null ? amount : BigDecimal.ZERO);
        vars.put("currency", currency);
        vars.put("catalogServiceCode", catalogServiceCode);
        putIfPresent(vars, context, "rail");
        putIfPresent(vars, context, "customerType");
        putIfPresent(vars, context, "agencyCode");
        putIfPresent(vars, context, "riskScore");

        if (dmnEvaluateService != null && StringUtils.hasText(policy.dmnDecisionKey())) {
            try {
                Map<String, Object> result = dmnEvaluateService.evaluate(policy.dmnDecisionKey(), vars);
                String code = extractProfileCode(result);
                if (StringUtils.hasText(code)) {
                    return code;
                }
            } catch (Exception e) {
                log.warn("DMN {} failed: {}", policy.dmnDecisionKey(), e.getMessage());
            }
            try {
                Map<String, Object> result = dmnEvaluateService.evaluate(
                        DmnTransferApprovalConfig.DECISION_NAME, vars);
                String code = extractProfileCode(result);
                if (StringUtils.hasText(code)) {
                    return code;
                }
            } catch (Exception e) {
                log.warn("DMN name evaluate failed: {}", e.getMessage());
            }
        }
        throw new BusinessException(
                "APPROVAL_PROFILE_UNRESOLVED",
                "Profil d'approbation introuvable — DMN obligatoire (pas de seuils hardcodés en fallback)");
    }

    private static String extractProfileCode(Map<String, Object> result) {
        if (result == null || result.isEmpty()) {
            return null;
        }
        Object v = result.get(DmnTransferApprovalConfig.OUT_PROFILE_CODE);
        if (v == null && result.size() == 1) {
            v = result.values().iterator().next();
        }
        return v != null ? String.valueOf(v).trim() : null;
    }

    private void applyNoApproval(Map<String, Object> processVarsOut, Map<String, Object> context) {
        processVarsOut.put(DmnTransferApprovalConfig.VAR_LEVELS_REQUIRED, 0);
        processVarsOut.put(DmnTransferApprovalConfig.VAR_PROFILE_CODE, DmnTransferApprovalConfig.PROFILE_NO_APPROVAL);
        processVarsOut.put("approvalRequired", false);
        processVarsOut.put(DmnTransferApprovalConfig.VAR_GROUP_L1, "");
        processVarsOut.put(DmnTransferApprovalConfig.VAR_GROUP_L2, "");
        processVarsOut.put(DmnTransferApprovalConfig.VAR_GROUP_L3, "");
        context.put(DmnTransferApprovalConfig.VAR_LEVELS_REQUIRED, 0);
        context.put(DmnTransferApprovalConfig.VAR_PROFILE_CODE, DmnTransferApprovalConfig.PROFILE_NO_APPROVAL);
        context.put("approvalRequired", false);
    }

    private void applyProfileVars(
            Map<String, Object> processVarsOut,
            Map<String, Object> context,
            ApprovalProfile profile,
            ApprovalInstance instance
    ) {
        int levels = profile.levelCount();
        processVarsOut.put(DmnTransferApprovalConfig.VAR_LEVELS_REQUIRED, levels);
        processVarsOut.put(DmnTransferApprovalConfig.VAR_PROFILE_CODE, profile.code());
        processVarsOut.put("approvalRequired", levels > 0);
        processVarsOut.put(DmnTransferApprovalConfig.VAR_APPROVAL_INSTANCE_ID, instance.getId().toString());

        String g1 = "";
        String g2 = "";
        String g3 = "";
        for (ApprovalLevel level : profile.levels()) {
            String csv = catalogSupport.candidateGroupsCsv(level);
            if (level.level() == 1) {
                g1 = csv;
            } else if (level.level() == 2) {
                g2 = csv;
            } else if (level.level() == 3) {
                g3 = csv;
            }
        }
        // Si levels sont indexés 1..n sans numéro explicite 3
        if (!profile.levels().isEmpty() && !StringUtils.hasText(g1)) {
            g1 = catalogSupport.candidateGroupsCsv(profile.levels().get(0));
        }
        if (profile.levels().size() > 1 && !StringUtils.hasText(g2)) {
            g2 = catalogSupport.candidateGroupsCsv(profile.levels().get(1));
        }
        if (profile.levels().size() > 2 && !StringUtils.hasText(g3)) {
            g3 = catalogSupport.candidateGroupsCsv(profile.levels().get(2));
        }

        processVarsOut.put(DmnTransferApprovalConfig.VAR_GROUP_L1, g1);
        processVarsOut.put(DmnTransferApprovalConfig.VAR_GROUP_L2, g2);
        processVarsOut.put(DmnTransferApprovalConfig.VAR_GROUP_L3, g3);

        context.putAll(processVarsOut);
    }

    private void assertMakerChecker(ApprovalPolicy policy, ApprovalInstance instance, String approverId) {
        if (policy.initiatorCanApprove()) {
            return;
        }
        if (StringUtils.hasText(instance.getInitiatedBy())
                && instance.getInitiatedBy().equalsIgnoreCase(approverId.trim())) {
            throw new BusinessException(
                    "SELF_APPROVAL_FORBIDDEN",
                    "L'initiateur ne peut pas valider son propre transfert",
                    HttpStatus.FORBIDDEN);
        }
    }

    private void assertSameUserAcrossLevels(
            ApprovalPolicy policy, ApprovalInstance instance, String approverId) {
        if (policy.sameUserAcrossLevelsAllowed()) {
            return;
        }
        List<ApprovalStepInstance> steps = stepRepository
                .findByApprovalInstanceIdOrderByLevelNumberAsc(instance.getId());
        for (ApprovalStepInstance s : steps) {
            for (ApprovalDecision d : decisionRepository.findByStepInstanceIdOrderByDecidedAtAsc(s.getId())) {
                if (approverId.equalsIgnoreCase(d.getApproverId())
                        && d.getOutcome() == ApprovalDecisionOutcome.APPROVE
                        && s.getStatus() == ApprovalStepStatus.APPROVED) {
                    throw new BusinessException(
                            "MULTI_LEVEL_SAME_USER_FORBIDDEN",
                            "Le même validateur ne peut pas approuver plusieurs niveaux",
                            HttpStatus.FORBIDDEN);
                }
            }
        }
    }

    private void assertFingerprint(
            ApprovalPolicy policy,
            ApprovalInstance instance,
            Map<String, Object> context,
            PaymentTransaction tx
    ) {
        if (!StringUtils.hasText(instance.getContextFingerprint())) {
            return;
        }
        String now = computeFingerprint(context, policy.criticalFields(), tx);
        if (!instance.getContextFingerprint().equals(now)) {
            instance.setStatus(ApprovalInstanceStatus.INVALIDATED);
            instance.setCompletedAt(Instant.now());
            instanceRepository.save(instance);
            throw new BusinessException(
                    "APPROVAL_CONTEXT_CHANGED",
                    "Données critiques modifiées — circuit d'approbation invalidé");
        }
    }

    private String computeFingerprint(
            Map<String, Object> context, List<String> fields, PaymentTransaction tx) {
        StringBuilder sb = new StringBuilder();
        for (String f : fields) {
            Object v = context.get(f);
            if ("amount".equals(f) && tx.getAmount() != null) {
                v = tx.getAmount();
            }
            if ("currency".equals(f) && tx.getCurrency() != null) {
                v = tx.getCurrency();
            }
            sb.append(f).append('=').append(v != null ? String.valueOf(v) : "").append('|');
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(dig);
        } catch (Exception e) {
            return Integer.toHexString(sb.toString().hashCode());
        }
    }

    private static String primaryRole(ApprovalStepInstance step) {
        return step.getCandidateGroups() != null
                ? step.getCandidateGroups().split(",")[0].trim()
                : null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<?, ?> m) {
        return (Map<String, Object>) m;
    }

    private static void putIfPresent(Map<String, Object> vars, Map<String, Object> context, String key) {
        Object v = context.get(key);
        if (v != null) {
            vars.put(key, v);
        }
    }

    private static BigDecimal toDecimal(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        if (v instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    /** Premier montant strictement &gt; 0 (évite de traiter 0 comme « déjà défini » sur facture). */
    private static BigDecimal firstPositive(BigDecimal... values) {
        if (values == null) {
            return null;
        }
        for (BigDecimal v : values) {
            if (v != null && v.compareTo(BigDecimal.ZERO) > 0) {
                return v;
            }
        }
        return null;
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

    public record ResolvedApproval(
            ApprovalPolicy policy,
            ApprovalProfile profile,
            ApprovalInstance instance
    ) {
    }
}
