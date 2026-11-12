package com.gateway.payment.configuration.api;

import com.gateway.payment.configuration.api.dto.ApiResponse;
import com.gateway.payment.configuration.api.dto.FseApiRequest;
import com.gateway.payment.configuration.api.dto.FseApiResponse;
import com.gateway.payment.configuration.service.ProviderApiService;
import com.gateway.payment.configuration.service.ProviderFseApiService;
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
@RequestMapping("/api/v1/providers/{providerId}/fse-apis")
@Tag(name = "Provider FSE APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProviderFseApiController {

    private final ProviderFseApiService fseApiService;
    private final ProviderApiService providerApiService;

    public ProviderFseApiController(ProviderFseApiService fseApiService, ProviderApiService providerApiService) {
        this.fseApiService = fseApiService;
        this.providerApiService = providerApiService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Lister les Api Omnicanal du fournisseur")
    public List<FseApiResponse> list(@PathVariable UUID providerId) {
        return fseApiService.listByProvider(providerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer une Api Omnicanal (connectivité)")
    public FseApiResponse create(
            @PathVariable UUID providerId,
            @Valid @RequestBody FseApiRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return fseApiService.create(providerId, request, principal.getUsername());
    }

    @PutMapping("/{fseApiId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Mettre à jour une Api Omnicanal")
    public FseApiResponse update(
            @PathVariable UUID providerId,
            @PathVariable UUID fseApiId,
            @Valid @RequestBody FseApiRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return fseApiService.update(providerId, fseApiId, request, principal.getUsername());
    }

    @GetMapping("/{fseApiId}/methods")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Lister les méthodes rattachées à une Api Omnicanal")
    public List<ApiResponse> listMethods(
            @PathVariable UUID providerId,
            @PathVariable UUID fseApiId
    ) {
        return providerApiService.listByFseApi(providerId, fseApiId);
    }
}
