package com.gateway.payment.configuration.api;

import com.gateway.payment.configuration.api.dto.WebhookConfigRequest;
import com.gateway.payment.configuration.api.dto.WebhookConfigResponse;
import com.gateway.payment.configuration.service.WebhookConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/providers/{providerId}/webhooks")
@Tag(name = "Webhook Config")
@SecurityRequirement(name = "bearerAuth")
public class ProviderWebhookConfigController {

    private final WebhookConfigService webhookConfigService;

    public ProviderWebhookConfigController(WebhookConfigService webhookConfigService) {
        this.webhookConfigService = webhookConfigService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Lister les configurations webhook (secrets masqués)")
    public List<WebhookConfigResponse> list(@PathVariable UUID providerId) {
        return webhookConfigService.listByProvider(providerId);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer / mettre à jour une configuration webhook")
    public WebhookConfigResponse upsert(
            @PathVariable UUID providerId,
            @Valid @RequestBody WebhookConfigRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return webhookConfigService.upsert(providerId, request, principal.getUsername());
    }
}
