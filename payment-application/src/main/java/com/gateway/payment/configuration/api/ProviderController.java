package com.gateway.payment.configuration.api;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.configuration.api.dto.ConnectivityTestResponse;
import com.gateway.payment.configuration.api.dto.CreateProviderFromTemplateRequest;
import com.gateway.payment.configuration.api.dto.OmnicanalBulkImportResponse;
import com.gateway.payment.configuration.api.dto.OmnicanalConnectivityImportRequest;
import com.gateway.payment.configuration.api.dto.OmnicanalConnectivityImportResponse;
import com.gateway.payment.configuration.api.dto.ProviderConfigExportResponse;
import com.gateway.payment.configuration.api.dto.ProviderConfigImportRequest;
import com.gateway.payment.configuration.api.dto.ProviderRequest;
import com.gateway.payment.configuration.api.dto.ProviderResponse;
import com.gateway.payment.configuration.api.dto.ProviderTemplateResponse;
import com.gateway.payment.configuration.service.OmnicanalConnectivityMigrationService;
import com.gateway.payment.configuration.service.ProviderConfigExportService;
import com.gateway.payment.configuration.service.ProviderConnectivityTestService;
import com.gateway.payment.configuration.service.ProviderService;
import com.gateway.payment.configuration.service.ProviderTemplateService;
import com.gateway.payment.orchestration.service.OmnicanalInterfaceImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/providers")
@Tag(name = "Providers")
@SecurityRequirement(name = "bearerAuth")
public class ProviderController {

    private final ProviderService providerService;
    private final ProviderConnectivityTestService connectivityTestService;
    private final ProviderTemplateService templateService;
    private final ProviderConfigExportService configExportService;
    private final OmnicanalConnectivityMigrationService omnicanalMigrationService;
    private final OmnicanalInterfaceImportService omnicanalInterfaceImportService;

    public ProviderController(
            ProviderService providerService,
            ProviderConnectivityTestService connectivityTestService,
            ProviderTemplateService templateService,
            ProviderConfigExportService configExportService,
            OmnicanalConnectivityMigrationService omnicanalMigrationService,
            OmnicanalInterfaceImportService omnicanalInterfaceImportService
    ) {
        this.providerService = providerService;
        this.connectivityTestService = connectivityTestService;
        this.templateService = templateService;
        this.configExportService = configExportService;
        this.omnicanalMigrationService = omnicanalMigrationService;
        this.omnicanalInterfaceImportService = omnicanalInterfaceImportService;
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les fournisseurs actifs (sélection paiement)")
    public List<ProviderResponse> listActive(
            @RequestParam(required = false) ServiceTypeCode serviceType
    ) {
        if (serviceType != null) {
            return providerService.listActiveByServiceType(serviceType);
        }
        return providerService.listActive();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Lister les fournisseurs (paginé, filtre optionnel q)")
    public Page<ProviderResponse> listAll(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "ordreAffichage", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return providerService.list(q, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer un fournisseur")
    public ProviderResponse create(
            @Valid @RequestBody ProviderRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return providerService.create(request, principal.getUsername());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Détail d'un fournisseur")
    public ProviderResponse get(@PathVariable UUID id) {
        return providerService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Mettre à jour un fournisseur")
    public ProviderResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody ProviderRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return providerService.update(id, request, principal.getUsername());
    }

    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Activer / désactiver un fournisseur")
    public ProviderResponse setEnabled(
            @PathVariable UUID id,
            @RequestParam boolean enabled,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return providerService.setEnabled(id, enabled, principal.getUsername());
    }

    @PostMapping("/{id}/test")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Tester la connectivité du fournisseur")
    public ConnectivityTestResponse test(@PathVariable UUID id) {
        return connectivityTestService.test(id);
    }

    @GetMapping("/templates")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Lister les modèles de fournisseurs disponibles")
    public List<ProviderTemplateResponse> listTemplates() {
        return templateService.listTemplates();
    }

    @PostMapping("/from-template")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer un fournisseur à partir d'un modèle")
    public ProviderResponse createFromTemplate(
            @Valid @RequestBody CreateProviderFromTemplateRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return templateService.createFromTemplate(request, principal.getUsername());
    }

    @GetMapping("/{id}/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Exporter la configuration complète d'un fournisseur (JSON)")
    public ProviderConfigExportResponse exportConfig(@PathVariable UUID id) {
        return configExportService.export(id);
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Importer une configuration fournisseur (JSON)")
    public ProviderResponse importConfig(
            @Valid @RequestBody ProviderConfigImportRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return configExportService.importConfig(request, principal.getUsername());
    }

    @PostMapping("/{id}/omnicanal-connectivity")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Importer la connectivité Omnicanal (Api/Methodes) sur DEVELOPPEMENT/PRODUCTION — sans toucher SANDBOX")
    public OmnicanalConnectivityImportResponse importOmnicanalConnectivity(
            @PathVariable UUID id,
            @Valid @RequestBody OmnicanalConnectivityImportRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return omnicanalMigrationService.importConnectivity(id, request, principal.getUsername());
    }

    @PostMapping("/omnicanal-connectivity/import-exported")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Importer toutes les fixtures Omnicanal exportées (DB → JSON)")
    public OmnicanalBulkImportResponse importExportedOmnicanalFixtures(
            @AuthenticationPrincipal UserDetails principal
    ) {
        return omnicanalMigrationService.importAllExportedFixtures(principal.getUsername());
    }

    @PostMapping("/omnicanal-interfaces/import-sonabel")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Importer InterfaceUtilisateur + ParametreUtilisateur SONABEL (0005 / 0029) depuis Omnicanal")
    public OmnicanalInterfaceImportService.ImportResult importSonabelInterfaces() {
        return omnicanalInterfaceImportService.importSonabel();
    }

    @PostMapping("/{id}/omnicanal-connectivity/seed")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Enregistrer les méthodes Omnicanal (fixture exportée ou legacy 0011/0004)")
    public OmnicanalConnectivityImportResponse seedOmnicanalConnectivity(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        ProviderResponse provider = providerService.getById(id);
        String fixture = omnicanalMigrationService.resolveFixtureClasspath(id)
                .orElseThrow(() -> new com.gateway.payment.common.exception.BusinessException(
                        "OMNI_FIXTURE_UNKNOWN",
                        "Aucune fixture Omnicanal pour ce fournisseur (codeFse " + provider.codeFse() + ")"));
        return omnicanalMigrationService.importConnectivity(
                id, omnicanalMigrationService.loadClasspath(fixture), principal.getUsername());
    }
}
