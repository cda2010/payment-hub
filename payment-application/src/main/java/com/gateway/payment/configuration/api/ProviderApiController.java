package com.gateway.payment.configuration.api;

import com.gateway.payment.configuration.api.dto.ApiRequest;
import com.gateway.payment.configuration.api.dto.ApiResponse;
import com.gateway.payment.configuration.service.ProviderApiService;
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
@RequestMapping("/api/v1/providers/{providerId}/apis")
@Tag(name = "Provider APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProviderApiController {

    private final ProviderApiService providerApiService;

    public ProviderApiController(ProviderApiService providerApiService) {
        this.providerApiService = providerApiService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Lister les APIs d'un fournisseur")
    public List<ApiResponse> list(@PathVariable UUID providerId) {
        return providerApiService.listByProvider(providerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer une API fournisseur")
    public ApiResponse create(
            @PathVariable UUID providerId,
            @Valid @RequestBody ApiRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return providerApiService.create(providerId, request, principal.getUsername());
    }

    @PutMapping("/{apiId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Mettre à jour une API fournisseur")
    public ApiResponse update(
            @PathVariable UUID providerId,
            @PathVariable UUID apiId,
            @Valid @RequestBody ApiRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return providerApiService.update(providerId, apiId, request, principal.getUsername());
    }
}
