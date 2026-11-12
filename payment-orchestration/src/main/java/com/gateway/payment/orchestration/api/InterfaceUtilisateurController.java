package com.gateway.payment.orchestration.api;

import com.gateway.payment.orchestration.api.dto.InterfaceTemplatePreviewResponse;
import com.gateway.payment.orchestration.api.dto.InterfaceUtilisateurRequest;
import com.gateway.payment.orchestration.api.dto.InterfaceUtilisateurResponse;
import com.gateway.payment.orchestration.api.dto.ParametreUtilisateurRequest;
import com.gateway.payment.orchestration.api.dto.ParametreUtilisateurResponse;
import com.gateway.payment.orchestration.service.InterfaceUtilisateurService;
import com.gateway.payment.orchestration.service.OrchestrationSeedService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/providers/{providerId}/interface-utilisateurs")
@Tag(name = "Orchestration — Interfaces utilisateur")
@SecurityRequirement(name = "bearerAuth")
public class InterfaceUtilisateurController {

    private final InterfaceUtilisateurService interfaceService;
    private final OrchestrationSeedService seedService;

    public InterfaceUtilisateurController(
            InterfaceUtilisateurService interfaceService,
            OrchestrationSeedService seedService
    ) {
        this.interfaceService = interfaceService;
        this.seedService = seedService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Lister les interfaces utilisateur du fournisseur (paginé)")
    public Page<InterfaceUtilisateurResponse> list(
            @PathVariable UUID providerId,
            @PageableDefault(size = 10, sort = "typeMethode", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return interfaceService.listByProvider(providerId, pageable);
    }

    @GetMapping("/templates")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Prévisualiser service, opération/API et paramètres par défaut pour un type méthode")
    public InterfaceTemplatePreviewResponse previewTemplate(
            @PathVariable UUID providerId,
            @RequestParam String typeMethode
    ) {
        return interfaceService.previewTemplate(providerId, typeMethode);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Détail interface utilisateur (avec ParametreUtilisateur)")
    public InterfaceUtilisateurResponse get(@PathVariable UUID providerId, @PathVariable UUID id) {
        return interfaceService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer une interface utilisateur (actif=true exige complétude)")
    public InterfaceUtilisateurResponse create(
            @PathVariable UUID providerId,
            @Valid @RequestBody InterfaceUtilisateurRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return interfaceService.create(providerId, request, principal.getUsername());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Mettre à jour une interface utilisateur")
    public InterfaceUtilisateurResponse update(
            @PathVariable UUID providerId,
            @PathVariable UUID id,
            @Valid @RequestBody InterfaceUtilisateurRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return interfaceService.update(providerId, id, request, principal.getUsername());
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Publier une interface (contrôle de complétude puis actif=true)")
    public InterfaceUtilisateurResponse publish(
            @PathVariable UUID providerId,
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return interfaceService.publish(providerId, id, principal.getUsername());
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Dépublier une interface (actif=false)")
    public InterfaceUtilisateurResponse unpublish(
            @PathVariable UUID providerId,
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return interfaceService.unpublish(providerId, id, principal.getUsername());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Supprimer une interface utilisateur")
    public void delete(
            @PathVariable UUID providerId,
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        interfaceService.delete(providerId, id, principal.getUsername());
    }

    @GetMapping("/{id}/parametres")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Lister les ParametreUtilisateur (constitues)")
    public List<ParametreUtilisateurResponse> listParametres(
            @PathVariable UUID providerId,
            @PathVariable UUID id
    ) {
        return interfaceService.listParametres(providerId, id);
    }

    @PostMapping("/{id}/parametres")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Ajouter un ParametreUtilisateur")
    public ParametreUtilisateurResponse addParametre(
            @PathVariable UUID providerId,
            @PathVariable UUID id,
            @Valid @RequestBody ParametreUtilisateurRequest request
    ) {
        return interfaceService.addParametre(providerId, id, request);
    }

    @PutMapping("/{id}/parametres/{paramId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Modifier un ParametreUtilisateur")
    public ParametreUtilisateurResponse updateParametre(
            @PathVariable UUID providerId,
            @PathVariable UUID id,
            @PathVariable UUID paramId,
            @Valid @RequestBody ParametreUtilisateurRequest request
    ) {
        return interfaceService.updateParametre(providerId, id, paramId, request);
    }

    @DeleteMapping("/{id}/parametres/{paramId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Supprimer un ParametreUtilisateur")
    public void deleteParametre(
            @PathVariable UUID providerId,
            @PathVariable UUID id,
            @PathVariable UUID paramId
    ) {
        interfaceService.deleteParametre(providerId, id, paramId);
    }

    @PostMapping("/{id}/parametres/seed-defaults")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Compléter les ParametreUtilisateur manquants depuis le catalogue")
    public InterfaceUtilisateurResponse seedDefaultParametres(
            @PathVariable UUID providerId,
            @PathVariable UUID id
    ) {
        return interfaceService.seedDefaultParametres(providerId, id);
    }

    @PostMapping("/from-api/{providerApiId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Générer une InterfaceUtilisateur REQUEST depuis une méthode (ProviderApi)")
    public InterfaceUtilisateurResponse generateFromApi(
            @PathVariable UUID providerId,
            @PathVariable UUID providerApiId
    ) {
        return interfaceService.generateFromProviderApi(providerId, providerApiId);
    }

    @PostMapping("/seed")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Générer une interface REQUEST (+ params défaut) pour chaque opération active du fournisseur")
    public Map<String, Object> seed(@PathVariable UUID providerId) {
        List<InterfaceUtilisateurResponse> before = interfaceService.listByProvider(providerId);
        List<String> upserted = seedService.seedInterfacesForProvider(providerId);
        return Map.of(
                "providerId", providerId,
                "interfacesBefore", before.size(),
                "typeMethodesUpserted", upserted
        );
    }
}
