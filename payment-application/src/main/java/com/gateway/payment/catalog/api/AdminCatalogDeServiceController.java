package com.gateway.payment.catalog.api;

import com.gateway.payment.catalog.api.dto.CatalogDeServiceRequest;
import com.gateway.payment.catalog.api.dto.CatalogDeServiceResponse;
import com.gateway.payment.catalog.api.dto.CatalogDeServiceVersionConfigRequest;
import com.gateway.payment.catalog.api.dto.CatalogDeServiceVersionRequest;
import com.gateway.payment.catalog.api.dto.CatalogDeServiceVersionResponse;
import com.gateway.payment.catalog.api.dto.CatalogProductCastingResponse;
import com.gateway.payment.catalog.api.dto.CatalogProductPublishRequest;
import com.gateway.payment.catalog.api.dto.CatalogProductPublishResponse;
import com.gateway.payment.catalog.api.dto.CatalogWorkflowBindingResponse;
import com.gateway.payment.catalog.api.dto.CatalogWorkflowStepReport;
import com.gateway.payment.catalog.service.CatalogDeServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/catalog-services")
@Tag(name = "Admin Catalogue de services")
@SecurityRequirement(name = "bearerAuth")
public class AdminCatalogDeServiceController {

    private final CatalogDeServiceService catalogDeServiceService;

    public AdminCatalogDeServiceController(CatalogDeServiceService catalogDeServiceService) {
        this.catalogDeServiceService = catalogDeServiceService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Lister le catalogue de services")
    public List<CatalogDeServiceResponse> list() {
        return catalogDeServiceService.listAll();
    }

    @GetMapping("/published")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les services PUBLISHED (filtre optionnel ?audience=RETAIL|CORPORATE|…)")
    public List<CatalogDeServiceResponse> listPublished(
            @RequestParam(required = false) String audience
    ) {
        return catalogDeServiceService.listPublished(audience);
    }

    @GetMapping("/published/{code}/casting")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Casting PUBLISHED (roles, participants, bindings) pour l'UI runtime")
    public CatalogProductCastingResponse publishedCasting(@PathVariable String code) {
        return catalogDeServiceService.getPublishedCasting(code);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public CatalogDeServiceResponse get(@PathVariable UUID id) {
        return catalogDeServiceService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer un service + version initiale (défaut 1.0)")
    public CatalogDeServiceResponse create(@Valid @RequestBody CatalogDeServiceRequest request) {
        return catalogDeServiceService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Mettre à jour les métadonnées produit (pas la config versionnée)")
    public CatalogDeServiceResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody CatalogDeServiceRequest request
    ) {
        return catalogDeServiceService.update(id, request);
    }

    @GetMapping("/{id}/versions")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Lister les versions (ex. 1.0, 1.1, 2.0)")
    public List<CatalogDeServiceVersionResponse> listVersions(@PathVariable UUID id) {
        return catalogDeServiceService.listVersions(id);
    }

    @PostMapping("/{id}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer une nouvelle version (modification importante)")
    public CatalogDeServiceVersionResponse createVersion(
            @PathVariable UUID id,
            @Valid @RequestBody CatalogDeServiceVersionRequest request
    ) {
        return catalogDeServiceService.createVersion(id, request);
    }

    @GetMapping("/versions/{versionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public CatalogDeServiceVersionResponse getVersion(@PathVariable UUID versionId) {
        return catalogDeServiceService.getVersion(versionId);
    }

    @PutMapping("/versions/{versionId}/configuration")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Mettre à jour configuration DRAFT (roles, participants, steps…)")
    public CatalogDeServiceVersionResponse updateDraftConfiguration(
            @PathVariable UUID versionId,
            @Valid @RequestBody CatalogDeServiceVersionConfigRequest request
    ) {
        return catalogDeServiceService.updateDraftConfiguration(versionId, request);
    }

    @PostMapping("/versions/{versionId}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Publier une version — workflowKey déployé + hôte payment_flow si hostFlow.autoCreate")
    public CatalogDeServiceVersionResponse publishVersion(@PathVariable UUID versionId) {
        return catalogDeServiceService.publishVersion(versionId);
    }

    @PostMapping("/products/publish")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "One-shot : créer/publier un produit + assurer hôte payment_flow (sans code Java)")
    public CatalogProductPublishResponse publishProduct(@Valid @RequestBody CatalogProductPublishRequest request) {
        return catalogDeServiceService.publishProduct(request);
    }

    @GetMapping("/versions/{versionId}/workflow")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Résoudre / valider le binding vers le process RapidBPMN")
    public CatalogWorkflowBindingResponse resolveWorkflow(@PathVariable UUID versionId) {
        return catalogDeServiceService.resolveWorkflow(versionId);
    }

    @GetMapping("/versions/{versionId}/workflow/steps")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Contrôler les serviceTasks du process (capability résolue, erreurs bloquantes, avertissements)")
    public CatalogWorkflowStepReport checkWorkflowSteps(@PathVariable UUID versionId) {
        return catalogDeServiceService.checkWorkflowSteps(versionId);
    }
}
