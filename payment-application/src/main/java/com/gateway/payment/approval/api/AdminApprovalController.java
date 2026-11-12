package com.gateway.payment.approval.api;

import com.gateway.payment.approval.api.dto.ApprovalDtos;
import com.gateway.payment.approval.service.ApprovalQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/approvals")
@Tag(name = "Admin Approvals")
@SecurityRequirement(name = "bearerAuth")
public class AdminApprovalController {

    private final ApprovalQueryService queryService;

    public AdminApprovalController(ApprovalQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/profiles")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public List<ApprovalDtos.ProfileResponse> listProfiles() {
        return queryService.listProfiles();
    }

    @GetMapping("/profiles/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public ApprovalDtos.ProfileResponse getProfile(@PathVariable UUID id) {
        return queryService.getProfile(id);
    }

    @PostMapping("/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public ApprovalDtos.ProfileResponse createProfile(@Valid @RequestBody ApprovalDtos.ProfileRequest request) {
        return queryService.createProfile(request);
    }

    @PutMapping("/profiles/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    public ApprovalDtos.ProfileResponse updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody ApprovalDtos.ProfileRequest request
    ) {
        return queryService.updateProfile(id, request);
    }

    @PostMapping("/simulate")
    @Operation(summary = "Simuler DMN → profil + niveaux (sans créer de paiement)")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public ApprovalDtos.SimulateResponse simulate(@RequestBody ApprovalDtos.SimulateRequest request) {
        return queryService.simulate(request != null ? request
                : new ApprovalDtos.SimulateRequest(null, null, null, null, null, null));
    }
}
