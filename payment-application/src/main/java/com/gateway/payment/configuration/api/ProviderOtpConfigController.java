package com.gateway.payment.configuration.api;

import com.gateway.payment.configuration.api.dto.OtpConfigRequest;
import com.gateway.payment.configuration.api.dto.OtpConfigResponse;
import com.gateway.payment.configuration.service.OtpConfigService;
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
@RequestMapping("/api/v1/providers/{providerId}/otp-config")
@Tag(name = "OTP Config")
@SecurityRequirement(name = "bearerAuth")
public class ProviderOtpConfigController {

    private final OtpConfigService otpConfigService;

    public ProviderOtpConfigController(OtpConfigService otpConfigService) {
        this.otpConfigService = otpConfigService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Consulter la configuration OTP")
    public OtpConfigResponse get(@PathVariable UUID providerId) {
        return otpConfigService.get(providerId);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer / mettre à jour la configuration OTP")
    public OtpConfigResponse upsert(
            @PathVariable UUID providerId,
            @Valid @RequestBody OtpConfigRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return otpConfigService.upsert(providerId, request, principal.getUsername());
    }
}
