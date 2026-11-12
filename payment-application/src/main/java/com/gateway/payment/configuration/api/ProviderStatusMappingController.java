package com.gateway.payment.configuration.api;

import com.gateway.payment.configuration.api.dto.StatusMappingRequest;
import com.gateway.payment.configuration.api.dto.StatusMappingResponse;
import com.gateway.payment.configuration.service.StatusMappingService;
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
@RequestMapping("/api/v1/providers/{providerId}/status-mappings")
@Tag(name = "Status Mappings")
@SecurityRequirement(name = "bearerAuth")
public class ProviderStatusMappingController {

    private final StatusMappingService statusMappingService;

    public ProviderStatusMappingController(StatusMappingService statusMappingService) {
        this.statusMappingService = statusMappingService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Lister les mappings de statut")
    public List<StatusMappingResponse> list(@PathVariable UUID providerId) {
        return statusMappingService.listByProvider(providerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer un mapping de statut")
    public StatusMappingResponse create(
            @PathVariable UUID providerId,
            @Valid @RequestBody StatusMappingRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return statusMappingService.create(providerId, request, principal.getUsername());
    }

    @PutMapping("/{mappingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Modifier un mapping de statut")
    public StatusMappingResponse update(
            @PathVariable UUID providerId,
            @PathVariable UUID mappingId,
            @Valid @RequestBody StatusMappingRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return statusMappingService.update(providerId, mappingId, request, principal.getUsername());
    }
}
