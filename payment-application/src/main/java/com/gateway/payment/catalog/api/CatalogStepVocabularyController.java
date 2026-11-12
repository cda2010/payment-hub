package com.gateway.payment.catalog.api;

import com.gateway.payment.catalog.api.dto.StepVocabularyResponse;
import com.gateway.payment.catalog.capability.CapabilityCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
@Tag(name = "Catalogue — vocabulaire des étapes")
@SecurityRequirement(name = "bearerAuth")
public class CatalogStepVocabularyController {

    private final CapabilityCatalogService capabilityCatalogService;

    public CatalogStepVocabularyController(CapabilityCatalogService capabilityCatalogService) {
        this.capabilityCatalogService = capabilityCatalogService;
    }

    @GetMapping("/capabilities")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Capabilities, opérations et rôles utilisables dans les attributs BPMN pay:*")
    public StepVocabularyResponse capabilities() {
        return capabilityCatalogService.vocabulary();
    }
}
