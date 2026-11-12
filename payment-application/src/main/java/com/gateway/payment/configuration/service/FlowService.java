package com.gateway.payment.configuration.service;

import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.api.dto.FlowRequest;
import com.gateway.payment.configuration.api.dto.FlowResponse;
import com.gateway.payment.configuration.api.dto.FlowStepRequest;
import com.gateway.payment.configuration.api.dto.FlowStepResponse;
import com.gateway.payment.configuration.domain.PaymentFlow;
import com.gateway.payment.configuration.domain.PaymentFlowStep;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.repository.PaymentFlowRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class FlowService {

    private final PaymentFlowRepository flowRepository;
    private final ProviderService providerService;
    private final AuditService auditService;

    public FlowService(
            PaymentFlowRepository flowRepository,
            ProviderService providerService,
            AuditService auditService
    ) {
        this.flowRepository = flowRepository;
        this.providerService = providerService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<FlowResponse> listByProvider(UUID providerId) {
        providerService.requireProvider(providerId);
        return flowRepository.findByProviderId(providerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FlowResponse getById(UUID flowId) {
        return toResponse(requireFlow(flowId));
    }

    @Transactional
    public FlowResponse create(UUID providerId, FlowRequest request, String actor) {
        PaymentProvider provider = providerService.requireProvider(providerId);
        flowRepository.findByProviderIdAndCode(providerId, request.code()).ifPresent(f -> {
            throw new BusinessException("FLOW_CODE_EXISTS", "Code flow déjà utilisé");
        });
        flowRepository.findByProviderIdAndServiceType(providerId, request.serviceType()).ifPresent(f -> {
            throw new BusinessException("FLOW_SERVICE_TYPE_EXISTS",
                    "Un flow existe déjà pour le type de service " + request.serviceType());
        });
        PaymentFlow flow = PaymentFlow.builder()
                .provider(provider)
                .code(request.code())
                .nom(request.nom())
                .serviceType(request.serviceType())
                .bpmProcessKey(blankToNull(request.bpmProcessKey()))
                .description(request.description())
                .actif(request.actif())
                .steps(new ArrayList<>())
                .build();
        PaymentFlow saved = flowRepository.save(flow);
        auditService.record(actor, "FLOW_CREATE", "FLOW", saved.getId().toString(),
                "Création flow " + saved.getCode() + " / " + saved.getServiceType());
        return toResponse(saved);
    }

    @Transactional
    public FlowResponse replaceSteps(UUID flowId, List<FlowStepRequest> steps, String actor) {
        PaymentFlow flow = requireFlow(flowId);
        flow.getSteps().clear();

        List<FlowStepRequest> ordered = steps == null ? List.of() : steps;
        for (FlowStepRequest stepRequest : ordered) {
            PaymentFlowStep step = PaymentFlowStep.builder()
                    .flow(flow)
                    .code(stepRequest.code())
                    .stepType(stepRequest.stepType())
                    .ordre(stepRequest.ordre())
                    .apiOperationCode(stepRequest.apiOperationCode())
                    .nextStepSuccess(stepRequest.nextStepSuccess())
                    .nextStepFailure(stepRequest.nextStepFailure())
                    .configurationJson(stepRequest.configurationJson())
                    .actif(stepRequest.actif())
                    .build();
            flow.getSteps().add(step);
        }

        PaymentFlow saved = flowRepository.save(flow);
        auditService.record(actor, "FLOW_STEPS_REPLACE", "FLOW", flowId.toString(),
                "Remplacement des étapes (" + saved.getSteps().size() + ")");
        return toResponse(saved);
    }

    private PaymentFlow requireFlow(UUID flowId) {
        return flowRepository.findById(flowId)
                .orElseThrow(() -> new BusinessException("FLOW_NOT_FOUND", "Flow introuvable", HttpStatus.NOT_FOUND));
    }

    private FlowResponse toResponse(PaymentFlow flow) {
        List<FlowStepResponse> steps = flow.getSteps().stream()
                .sorted(Comparator.comparing(PaymentFlowStep::getOrdre, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toStepResponse)
                .toList();
        return new FlowResponse(
                flow.getId(),
                flow.getProvider().getId(),
                flow.getCode(),
                flow.getNom(),
                flow.getServiceType(),
                flow.getBpmProcessKey(),
                flow.getDescription(),
                flow.isActif(),
                flow.getCreatedAt(),
                steps
        );
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private FlowStepResponse toStepResponse(PaymentFlowStep step) {
        return new FlowStepResponse(
                step.getId(),
                step.getFlow().getId(),
                step.getCode(),
                step.getStepType(),
                step.getOrdre(),
                step.getApiOperationCode(),
                step.getNextStepSuccess(),
                step.getNextStepFailure(),
                step.getConfigurationJson(),
                step.isActif()
        );
    }
}
