package com.gateway.payment.configuration.api;

import com.gateway.payment.configuration.api.dto.OperationRequest;
import com.gateway.payment.configuration.api.dto.OperationResponse;
import com.gateway.payment.configuration.service.OperationService;
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
@RequestMapping("/api/v1/providers/{providerId}/operations")
@Tag(name = "Provider Operations")
@SecurityRequirement(name = "bearerAuth")
public class ProviderOperationController {

    private final OperationService operationService;

    public ProviderOperationController(OperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Lister les opérations d'un fournisseur")
    public List<OperationResponse> list(@PathVariable UUID providerId) {
        return operationService.listByProvider(providerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer une opération")
    public OperationResponse create(
            @PathVariable UUID providerId,
            @Valid @RequestBody OperationRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return operationService.create(providerId, request, principal.getUsername());
    }

    @PutMapping("/{opId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Mettre à jour une opération")
    public OperationResponse update(
            @PathVariable UUID providerId,
            @PathVariable UUID opId,
            @Valid @RequestBody OperationRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return operationService.update(providerId, opId, request, principal.getUsername());
    }
}
