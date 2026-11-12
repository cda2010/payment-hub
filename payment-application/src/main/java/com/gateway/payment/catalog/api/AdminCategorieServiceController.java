package com.gateway.payment.catalog.api;

import com.gateway.payment.catalog.api.dto.CategorieServiceRequest;
import com.gateway.payment.catalog.api.dto.CategorieServiceResponse;
import com.gateway.payment.catalog.service.CategorieServiceService;
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
@RequestMapping("/api/v1/admin/catalog-categories")
@Tag(name = "Admin Catégories de services")
@SecurityRequirement(name = "bearerAuth")
public class AdminCategorieServiceController {

    private final CategorieServiceService categorieServiceService;

    public AdminCategorieServiceController(CategorieServiceService categorieServiceService) {
        this.categorieServiceService = categorieServiceService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    @Operation(summary = "Lister les catégories de services")
    public List<CategorieServiceResponse> list() {
        return categorieServiceService.listAll();
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les catégories actives")
    public List<CategorieServiceResponse> listActive() {
        return categorieServiceService.listActive();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'AUDITOR')")
    public CategorieServiceResponse get(@PathVariable UUID id) {
        return categorieServiceService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer une catégorie de services")
    public CategorieServiceResponse create(@Valid @RequestBody CategorieServiceRequest request) {
        return categorieServiceService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Mettre à jour une catégorie")
    public CategorieServiceResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody CategorieServiceRequest request
    ) {
        return categorieServiceService.update(id, request);
    }
}
