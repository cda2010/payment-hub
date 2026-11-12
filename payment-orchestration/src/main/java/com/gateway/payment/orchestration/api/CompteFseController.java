package com.gateway.payment.orchestration.api;

import com.gateway.payment.orchestration.api.dto.CompteFseRequest;
import com.gateway.payment.orchestration.api.dto.CompteFseResponse;
import com.gateway.payment.orchestration.api.dto.TypeCompteResponse;
import com.gateway.payment.orchestration.service.CompteFseAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/orchestration/comptes-fse")
@Tag(name = "Orchestration — Comptes FSE (catalogue)")
@SecurityRequirement(name = "bearerAuth")
public class CompteFseController {

    private final CompteFseAdminService compteService;

    public CompteFseController(CompteFseAdminService compteService) {
        this.compteService = compteService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Lister les comptes catalogue")
    public List<CompteFseResponse> list() {
        return compteService.listAll();
    }

    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Lister les types de compte (CPTELORO, …)")
    public List<TypeCompteResponse> listTypes() {
        return compteService.listTypes();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Détail d'un compte catalogue")
    public CompteFseResponse get(@PathVariable UUID id) {
        return compteService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer un compte catalogue")
    public CompteFseResponse create(@Valid @RequestBody CompteFseRequest request) {
        return compteService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Mettre à jour un compte catalogue")
    public CompteFseResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody CompteFseRequest request
    ) {
        return compteService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Supprimer un compte catalogue")
    public void delete(@PathVariable UUID id) {
        compteService.delete(id);
    }
}
