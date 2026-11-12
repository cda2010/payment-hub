package com.gateway.payment.orchestration.api;

import com.gateway.payment.orchestration.api.dto.RelationFseCorridorRequest;
import com.gateway.payment.orchestration.api.dto.RelationFseCorridorResponse;
import com.gateway.payment.orchestration.service.RelationFseCorridorService;
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
@RequestMapping("/api/v1/orchestration/corridors-fse")
@Tag(name = "Orchestration — Corridors FSE (LORO / MIROIR)")
@SecurityRequirement(name = "bearerAuth")
public class RelationFseCorridorController {

    private final RelationFseCorridorService corridorService;

    public RelationFseCorridorController(RelationFseCorridorService corridorService) {
        this.corridorService = corridorService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Lister les corridors inter-FSE")
    public List<RelationFseCorridorResponse> list() {
        return corridorService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT')")
    @Operation(summary = "Détail d'un corridor")
    public RelationFseCorridorResponse get(@PathVariable UUID id) {
        return corridorService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Créer un corridor (optionnellement le sens inverse)")
    public List<RelationFseCorridorResponse> create(@Valid @RequestBody RelationFseCorridorRequest request) {
        return corridorService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Mettre à jour un corridor et ses comptes LORO/MIROIR")
    public RelationFseCorridorResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody RelationFseCorridorRequest request
    ) {
        return corridorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER')")
    @Operation(summary = "Supprimer un corridor et ses comptes")
    public void delete(@PathVariable UUID id) {
        corridorService.delete(id);
    }
}
