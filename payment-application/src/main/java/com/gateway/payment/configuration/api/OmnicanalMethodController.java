package com.gateway.payment.configuration.api;

import com.gateway.payment.configuration.api.dto.OmnicanalMethodCreateRequest;
import com.gateway.payment.configuration.api.dto.OmnicanalMethodCreateResponse;
import com.gateway.payment.configuration.service.OmnicanalMethodService;
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
@RequestMapping("/api/v1/providers")
@Tag(name = "Omnicanal Methods")
@SecurityRequirement(name = "bearerAuth")
public class OmnicanalMethodController {

    private final OmnicanalMethodService methodService;

    public OmnicanalMethodController(OmnicanalMethodService methodService) {
        this.methodService = methodService;
    }

    @GetMapping("/omnicanal-type-methodes")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Catalogue TypeMethode Omnicanal")
    public List<String> listTypeMethodes() {
        return methodService.listTypeMethodes();
    }

    @PostMapping("/{providerId}/methods")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Ajouter une méthode Omnicanal rattachée à une Api (fseApiId)")
    public OmnicanalMethodCreateResponse createMethod(
            @PathVariable UUID providerId,
            @Valid @RequestBody OmnicanalMethodCreateRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return methodService.createMethod(providerId, request, principal.getUsername());
    }
}
