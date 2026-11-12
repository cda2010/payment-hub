package com.gateway.payment.configuration.api;

import com.gateway.payment.configuration.api.dto.EnvironmentRequest;
import com.gateway.payment.configuration.api.dto.EnvironmentResponse;
import com.gateway.payment.configuration.service.EnvironmentService;
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
@RequestMapping("/api/v1/providers/{providerId}/environments")
@Tag(name = "Provider Environments")
@SecurityRequirement(name = "bearerAuth")
public class ProviderEnvironmentController {

    private final EnvironmentService environmentService;

    public ProviderEnvironmentController(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Lister les environnements d'un fournisseur")
    public List<EnvironmentResponse> list(@PathVariable UUID providerId) {
        return environmentService.listByProvider(providerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer un environnement")
    public EnvironmentResponse create(
            @PathVariable UUID providerId,
            @Valid @RequestBody EnvironmentRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return environmentService.create(providerId, request, principal.getUsername());
    }

    @PutMapping("/{envId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Mettre à jour un environnement")
    public EnvironmentResponse update(
            @PathVariable UUID providerId,
            @PathVariable UUID envId,
            @Valid @RequestBody EnvironmentRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return environmentService.update(providerId, envId, request, principal.getUsername());
    }
}
