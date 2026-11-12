package com.gateway.payment.configuration.api;

import com.gateway.payment.configuration.api.dto.AuthenticationRequest;
import com.gateway.payment.configuration.api.dto.AuthenticationResponse;
import com.gateway.payment.configuration.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/providers/{providerId}/authentication")
@Tag(name = "Provider Authentication")
@SecurityRequirement(name = "bearerAuth")
public class ProviderAuthenticationController {

    private final AuthenticationService authenticationService;

    public ProviderAuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Consulter l'authentification (secrets masqués)")
    public AuthenticationResponse get(
            @PathVariable UUID providerId,
            @RequestParam UUID environmentId
    ) {
        return authenticationService.get(providerId, environmentId);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer / mettre à jour l'authentification")
    public AuthenticationResponse upsert(
            @PathVariable UUID providerId,
            @Valid @RequestBody AuthenticationRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return authenticationService.upsert(providerId, request, principal.getUsername());
    }
}
