package com.gateway.payment.approval.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.approval.api.dto.ApprovalDtos;
import com.gateway.payment.approval.domain.ApprovalDecision;
import com.gateway.payment.approval.domain.ApprovalInstance;
import com.gateway.payment.approval.domain.ApprovalStepInstance;
import com.gateway.payment.approval.domain.ApprovalStepStatus;
import com.gateway.payment.approval.domain.ApprovalProfileEntity;
import com.gateway.payment.approval.model.ApprovalLevel;
import com.gateway.payment.approval.model.ApprovalProfile;
import com.gateway.payment.approval.repository.ApprovalDecisionRepository;
import com.gateway.payment.approval.repository.ApprovalInstanceRepository;
import com.gateway.payment.approval.repository.ApprovalProfileRepository;
import com.gateway.payment.approval.repository.ApprovalStepInstanceRepository;
import com.gateway.payment.bpm.dmn.DmnTransferApprovalConfig;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import com.rapidframe.bpm.dmn.DmnEvaluateService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ApprovalQueryService {

    private final ApprovalStepInstanceRepository stepRepository;
    private final ApprovalInstanceRepository instanceRepository;
    private final ApprovalDecisionRepository decisionRepository;
    private final ApprovalProfileRepository profileRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final ApprovalIdentitySupport identitySupport;
    private final ApprovalCatalogSupport catalogSupport;
    private final DmnEvaluateService dmnEvaluateService;
    private final ObjectMapper objectMapper;

    public ApprovalQueryService(
            ApprovalStepInstanceRepository stepRepository,
            ApprovalInstanceRepository instanceRepository,
            ApprovalDecisionRepository decisionRepository,
            ApprovalProfileRepository profileRepository,
            PaymentTransactionRepository transactionRepository,
            ApprovalIdentitySupport identitySupport,
            ApprovalCatalogSupport catalogSupport,
            ObjectProvider<DmnEvaluateService> dmnEvaluateService,
            ObjectMapper objectMapper
    ) {
        this.stepRepository = stepRepository;
        this.instanceRepository = instanceRepository;
        this.decisionRepository = decisionRepository;
        this.profileRepository = profileRepository;
        this.transactionRepository = transactionRepository;
        this.identitySupport = identitySupport;
        this.catalogSupport = catalogSupport;
        this.dmnEvaluateService = dmnEvaluateService.getIfAvailable();
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<ApprovalDtos.InboxItem> inbox() {
        List<ApprovalStepInstance> waiting = stepRepository.findByStatusOrderByCreatedAtAsc(ApprovalStepStatus.WAITING);
        List<ApprovalDtos.InboxItem> out = new ArrayList<>();
        for (ApprovalStepInstance step : waiting) {
            if (!identitySupport.matchesCandidateGroups(step.getCandidateGroups())) {
                continue;
            }
            ApprovalInstance instance = step.getApprovalInstance();
            PaymentTransaction tx = transactionRepository.findByPaymentId(instance.getPaymentId()).orElse(null);
            out.add(new ApprovalDtos.InboxItem(
                    step.getId(),
                    instance.getId(),
                    instance.getPaymentId(),
                    instance.getProfileCode(),
                    step.getLevelNumber(),
                    step.getRequiredApprovals(),
                    step.getCollectedApprovals(),
                    step.getCandidateGroups(),
                    instance.getInitiatedBy(),
                    tx != null ? tx.getAmount() : null,
                    tx != null ? tx.getCurrency() : null,
                    tx != null && tx.getStatus() != null ? tx.getStatus().name() : null,
                    step.getCreatedAt()
            ));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public ApprovalDtos.Detail detailByPaymentId(String paymentId) {
        ApprovalInstance instance = instanceRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new BusinessException("APPROVAL_NOT_FOUND",
                        "Aucune approbation pour " + paymentId, HttpStatus.NOT_FOUND));
        return toDetail(instance);
    }

    @Transactional(readOnly = true)
    public ApprovalDtos.Detail detailByInstanceId(UUID id) {
        ApprovalInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("APPROVAL_NOT_FOUND",
                        "Instance introuvable", HttpStatus.NOT_FOUND));
        return toDetail(instance);
    }

    private ApprovalDtos.Detail toDetail(ApprovalInstance instance) {
        PaymentTransaction tx = transactionRepository.findByPaymentId(instance.getPaymentId()).orElse(null);
        List<ApprovalStepInstance> steps = stepRepository
                .findByApprovalInstanceIdOrderByLevelNumberAsc(instance.getId());
        List<ApprovalDtos.StepView> stepViews = new ArrayList<>();
        String rejectionComment = null;
        for (ApprovalStepInstance s : steps) {
            List<ApprovalDecision> decisions = decisionRepository.findByStepInstanceIdOrderByDecidedAtAsc(s.getId());
            List<ApprovalDtos.DecisionView> dvs = decisions.stream()
                    .map(d -> new ApprovalDtos.DecisionView(
                            d.getId(), d.getApproverId(), d.getApproverRole(),
                            d.getOutcome() != null ? d.getOutcome().name() : null,
                            d.getComment(), d.getDecidedAt()))
                    .toList();
            if (rejectionComment == null) {
                rejectionComment = dvs.stream()
                        .filter(d -> "REJECT".equals(d.outcome()))
                        .map(ApprovalDtos.DecisionView::comment)
                        .filter(StringUtils::hasText)
                        .findFirst()
                        .orElse(null);
            }
            stepViews.add(new ApprovalDtos.StepView(
                    s.getId(), s.getLevelNumber(),
                    s.getStatus() != null ? s.getStatus().name() : null,
                    s.getRequiredApprovals(), s.getCollectedApprovals(),
                    s.getCandidateGroups(), s.getBpmTaskDefinitionKey(), dvs));
        }
        return new ApprovalDtos.Detail(
                instance.getId(),
                instance.getPaymentId(),
                instance.getProfileCode(),
                instance.getPolicyCode(),
                instance.getStrategy(),
                instance.getStatus() != null ? instance.getStatus().name() : null,
                instance.getCurrentLevel(),
                instance.getTotalLevels(),
                instance.getInitiatedBy(),
                instance.getCreatedAt(),
                instance.getCompletedAt(),
                tx != null ? tx.getAmount() : null,
                tx != null ? tx.getCurrency() : null,
                tx != null && tx.getStatus() != null ? tx.getStatus().name() : null,
                rejectionComment,
                stepViews
        );
    }

    /**
     * TX writable : Flowable DMN persiste un audit (INSERT) lors de {@code evaluate}.
     * En readOnly, Postgres refuse l'INSERT et abort toute la transaction → 500.
     */
    @Transactional
    public ApprovalDtos.SimulateResponse simulate(ApprovalDtos.SimulateRequest request) {
        BigDecimal amount = request.amount() != null ? request.amount() : BigDecimal.ZERO;
        String currency = StringUtils.hasText(request.currency()) ? request.currency() : "XOF";
        String catalog = StringUtils.hasText(request.catalogServiceCode())
                ? request.catalogServiceCode() : "ACCOUNT_TRANSFER";

        Map<String, Object> vars = new HashMap<>();
        vars.put("amount", amount);
        vars.put("currency", currency);
        vars.put("catalogServiceCode", catalog);
        if (StringUtils.hasText(request.rail())) {
            vars.put("rail", request.rail());
        }
        if (StringUtils.hasText(request.customerType())) {
            vars.put("customerType", request.customerType());
        }
        if (StringUtils.hasText(request.agencyCode())) {
            vars.put("agencyCode", request.agencyCode());
        }

        String profileCode = null;
        String source = "fallback";
        if (dmnEvaluateService != null) {
            try {
                Map<String, Object> result = dmnEvaluateService.evaluate(
                        DmnTransferApprovalConfig.DECISION_KEY, vars);
                Object code = result != null ? result.get(DmnTransferApprovalConfig.OUT_PROFILE_CODE) : null;
                if (code != null && StringUtils.hasText(String.valueOf(code))) {
                    profileCode = String.valueOf(code).trim();
                    source = "dmn";
                }
            } catch (Exception ignored) {
                // fallback below
            }
        }
        if (!StringUtils.hasText(profileCode)) {
            throw new BusinessException(
                    "APPROVAL_PROFILE_UNRESOLVED",
                    "Profil d'approbation introuvable — DMN obligatoire (simulation sans seuils hardcodés)");
        }

        ApprovalProfile profile = catalogSupport.loadProfile(profileCode);
        List<ApprovalDtos.SimulateLevel> levels = new ArrayList<>();
        for (ApprovalLevel level : profile.levels()) {
            List<String> assignments = level.assignments().stream()
                    .map(a -> a.type() + ":" + a.value())
                    .toList();
            levels.add(new ApprovalDtos.SimulateLevel(level.level(), level.requiredApprovals(), assignments));
        }
        return new ApprovalDtos.SimulateResponse(
                profile.code(), profile.strategy(), profile.levelCount(), levels, source);
    }

    @Transactional(readOnly = true)
    public List<ApprovalDtos.ProfileResponse> listProfiles() {
        return profileRepository.findAll().stream()
                .map(this::toProfileResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApprovalDtos.ProfileResponse getProfile(UUID id) {
        return profileRepository.findById(id)
                .map(this::toProfileResponse)
                .orElseThrow(() -> new BusinessException("PROFILE_NOT_FOUND", "Profil introuvable", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public ApprovalDtos.ProfileResponse createProfile(ApprovalDtos.ProfileRequest request) {
        validateDefinition(request.definitionJson());
        if (profileRepository.findByCodeIgnoreCase(request.code()).isPresent()) {
            throw new BusinessException("PROFILE_EXISTS", "Code profil déjà utilisé");
        }
        var entity = ApprovalProfileEntity.builder()
                .code(request.code().trim().toUpperCase())
                .name(request.name())
                .description(request.description())
                .definitionJson(request.definitionJson())
                .actif(request.actif() == null || request.actif())
                .build();
        return toProfileResponse(profileRepository.save(entity));
    }

    @Transactional
    public ApprovalDtos.ProfileResponse updateProfile(UUID id, ApprovalDtos.ProfileRequest request) {
        ApprovalProfileEntity entity = profileRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PROFILE_NOT_FOUND", "Profil introuvable", HttpStatus.NOT_FOUND));
        if (StringUtils.hasText(request.definitionJson())) {
            validateDefinition(request.definitionJson());
            entity.setDefinitionJson(request.definitionJson());
        }
        if (StringUtils.hasText(request.name())) {
            entity.setName(request.name());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.actif() != null) {
            entity.setActif(request.actif());
        }
        if (StringUtils.hasText(request.code()) && !request.code().equalsIgnoreCase(entity.getCode())) {
            if (profileRepository.findByCodeIgnoreCase(request.code()).isPresent()) {
                throw new BusinessException("PROFILE_EXISTS", "Code profil déjà utilisé");
            }
            entity.setCode(request.code().trim().toUpperCase());
        }
        return toProfileResponse(profileRepository.save(entity));
    }

    private void validateDefinition(String json) {
        if (!StringUtils.hasText(json)) {
            throw new BusinessException("DEFINITION_REQUIRED", "definitionJson requis");
        }
        try {
            objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new BusinessException("DEFINITION_INVALID", "definitionJson JSON invalide");
        }
    }

    private ApprovalDtos.ProfileResponse toProfileResponse(ApprovalProfileEntity e) {
        return new ApprovalDtos.ProfileResponse(
                e.getId(), e.getCode(), e.getName(), e.getDescription(),
                e.getDefinitionJson(), e.isActif(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
