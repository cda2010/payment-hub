package com.gateway.payment.configuration.api;

import com.gateway.payment.configuration.api.dto.FlowRequest;
import com.gateway.payment.configuration.api.dto.FlowResponse;
import com.gateway.payment.configuration.api.dto.FlowStepRequest;
import com.gateway.payment.configuration.service.FlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Payment Flows")
@SecurityRequirement(name = "bearerAuth")
public class PaymentFlowController {

    private final FlowService flowService;

    public PaymentFlowController(FlowService flowService) {
        this.flowService = flowService;
    }

    @GetMapping("/api/v1/providers/{providerId}/flows")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Lister les flows d'un fournisseur")
    public List<FlowResponse> listByProvider(@PathVariable UUID providerId) {
        return flowService.listByProvider(providerId);
    }

    @PostMapping("/api/v1/providers/{providerId}/flows")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer un flow")
    public FlowResponse create(
            @PathVariable UUID providerId,
            @Valid @RequestBody FlowRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return flowService.create(providerId, request, principal.getUsername());
    }

    @GetMapping("/api/v1/flows/{flowId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Détail d'un flow avec ses étapes")
    public FlowResponse get(@PathVariable UUID flowId) {
        return flowService.getById(flowId);
    }

    @PutMapping("/api/v1/flows/{flowId}/steps")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Remplacer atomiquement les étapes d'un flow")
    public FlowResponse replaceSteps(
            @PathVariable UUID flowId,
            @Valid @RequestBody List<FlowStepRequest> steps,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return flowService.replaceSteps(flowId, steps, principal.getUsername());
    }
}
