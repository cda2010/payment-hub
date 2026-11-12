package com.gateway.payment.orchestration.api;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.orchestration.api.dto.*;
import com.gateway.payment.orchestration.domain.SchemaPatternCode;
import com.gateway.payment.orchestration.domain.StepPolicyCatalog;
import com.gateway.payment.orchestration.service.LegCompilerService;
import com.gateway.payment.orchestration.service.SchemaEcritureService;
import com.gateway.payment.orchestration.service.SchemaMatchingService;
import com.gateway.payment.orchestration.service.SchemaOrchestrationService;
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
@RequestMapping("/api/v1/orchestration")
@Tag(name = "Orchestration — Schémas d'écriture")
@SecurityRequirement(name = "bearerAuth")
public class SchemaEcritureController {

    private final SchemaEcritureService schemaService;
    private final SchemaMatchingService matchingService;
    private final LegCompilerService legCompilerService;
    private final SchemaOrchestrationService orchestrationService;

    public SchemaEcritureController(
            SchemaEcritureService schemaService,
            SchemaMatchingService matchingService,
            LegCompilerService legCompilerService,
            SchemaOrchestrationService orchestrationService
    ) {
        this.schemaService = schemaService;
        this.matchingService = matchingService;
        this.legCompilerService = legCompilerService;
        this.orchestrationService = orchestrationService;
    }

    @GetMapping("/schema-ecritures")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Lister les schémas d'écriture")
    public List<SchemaEcritureResponse> list(
            @RequestParam(required = false) ServiceTypeCode serviceType
    ) {
        if (serviceType != null) {
            return schemaService.listByServiceType(serviceType);
        }
        return schemaService.listAll();
    }

    @GetMapping("/schema-ecritures/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Détail schéma avec lignes et paramètres")
    public SchemaEcritureResponse get(@PathVariable UUID id) {
        return schemaService.getById(id);
    }

    @PostMapping("/schema-ecritures")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer un schéma d'écriture (toujours inactif jusqu'à publication)")
    public SchemaEcritureResponse create(
            @Valid @RequestBody SchemaEcritureRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return schemaService.create(request, principal.getUsername());
    }

    @PutMapping("/schema-ecritures/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Mettre à jour un schéma d'écriture (actif=true exige un schéma complet)")
    public SchemaEcritureResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody SchemaEcritureRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return schemaService.update(id, request, principal.getUsername());
    }

    @PostMapping("/schema-ecritures/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Publier un schéma (contrôle de complétude puis actif=true)")
    public SchemaEcritureResponse publish(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return schemaService.publish(id, principal.getUsername());
    }

    @PostMapping("/schema-ecritures/{id}/unpublish")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Dépublier un schéma (actif=false)")
    public SchemaEcritureResponse unpublish(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return schemaService.unpublish(id, principal.getUsername());
    }

    @DeleteMapping("/schema-ecritures/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Supprimer un schéma d'écriture")
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal UserDetails principal) {
        schemaService.delete(id, principal.getUsername());
    }

    @GetMapping("/schema-ecritures/{schemaId}/lignes")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Lister les lignes d'un schéma")
    public List<LigneSchemaEcritureResponse> listLignes(@PathVariable UUID schemaId) {
        return schemaService.listLignes(schemaId);
    }

    @PostMapping("/ligne-schema-ecritures")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Ajouter une ligne de schéma")
    public LigneSchemaEcritureResponse addLigne(@Valid @RequestBody LigneSchemaEcritureRequest request) {
        return schemaService.addLigne(request);
    }

    @PutMapping("/ligne-schema-ecritures/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Mettre à jour une ligne de schéma")
    public LigneSchemaEcritureResponse updateLigne(
            @PathVariable UUID id,
            @Valid @RequestBody LigneSchemaEcritureRequest request
    ) {
        return schemaService.updateLigne(id, request);
    }

    @DeleteMapping("/ligne-schema-ecritures/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Supprimer une ligne de schéma")
    public void deleteLigne(@PathVariable UUID id) {
        schemaService.deleteLigne(id);
    }

    @PostMapping("/parametre-schemas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Ajouter un paramètre schéma")
    public ParametreSchemaResponse addParametre(@Valid @RequestBody ParametreSchemaRequest request) {
        return schemaService.addParametre(request);
    }

    @DeleteMapping("/parametre-schemas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Supprimer un paramètre schéma")
    public void deleteParametre(@PathVariable UUID id) {
        schemaService.deleteParametre(id);
    }

    @PutMapping("/parametre-schemas/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Mettre à jour un paramètre schéma")
    public ParametreSchemaResponse updateParametre(
            @PathVariable UUID id,
            @Valid @RequestBody ParametreSchemaRequest request
    ) {
        return schemaService.updateParametre(id, request);
    }

    @GetMapping("/parametre-schemas/ligne/{ligneId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Lister les paramètres d'une ligne")
    public List<ParametreSchemaResponse> listParametres(@PathVariable UUID ligneId) {
        return schemaService.listParametres(ligneId);
    }

    @GetMapping("/ligne-schema-ecritures/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Détail d'une ligne de schéma")
    public LigneSchemaEcritureResponse getLigne(@PathVariable UUID id) {
        return schemaService.getLigne(id);
    }

    @GetMapping("/catalog")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Catalogues SchemaEcrit / LigneSchemaEcrit (listes UI)")
    public OrchestrationCatalogResponse catalog() {
        return new OrchestrationCatalogResponse(
                SchemaPatternCode.all(),
                StepPolicyCatalog.TYPE_METHODES,
                StepPolicyCatalog.EXECUTION_MODES,
                StepPolicyCatalog.TIMEOUT_POLICIES,
                StepPolicyCatalog.CALLBACK_POLICIES,
                StepPolicyCatalog.RETRY_POLICIES,
                StepPolicyCatalog.IDEMPOTENCY_POLICIES,
                StepPolicyCatalog.COMPENSATION_POLICIES,
                StepPolicyCatalog.COMPENSATION_MODES,
                StepPolicyCatalog.CRITICALITIES,
                StepPolicyCatalog.ERROR_ACTIONS,
                StepPolicyCatalog.DEPENDENCY_CONDITIONS,
                StepPolicyCatalog.PARAM_TYPES
        );
    }

    @GetMapping("/pattern-codes")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Codes pattern DMN disponibles")
    public List<String> patternCodes() {
        return SchemaPatternCode.all();
    }

    @PostMapping("/match-schema")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Matcher un schéma pour une paire FSE source/dest")
    public SchemaMatchResponse matchSchema(@Valid @RequestBody SchemaMatchRequest request) {
        return matchingService.match(request);
    }

    @PostMapping("/compile-leg")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Compiler une jambe FSE (preview LegCompiler)")
    public LegCompileResponse compileLeg(@Valid @RequestBody LegCompileRequest request) {
        return legCompilerService.compile(request);
    }

    @PostMapping("/execute-leg")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Exécuter une jambe FSE (InterfaceUtilisateur + appel provider)")
    public SchemaLegResult executeLeg(@Valid @RequestBody LegExecuteRequest request) {
        return orchestrationService.executeLeg(request);
    }

    @PostMapping("/execute-schema")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Exécuter un schéma d'écriture (runtime LigneSchemaEcrit)")
    public SchemaExecuteResponse executeSchema(@Valid @RequestBody SchemaExecuteRequest request) {
        return orchestrationService.execute(request);
    }
}
