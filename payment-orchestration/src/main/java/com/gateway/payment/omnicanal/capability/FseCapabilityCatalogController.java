package com.gateway.payment.omnicanal.capability;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/canal/capabilities")
@Tag(name = "Canal — Capacités FSE")
@SecurityRequirement(name = "bearerAuth")
public class FseCapabilityCatalogController {

    private final FseCapabilityCatalogService catalogService;

    public FseCapabilityCatalogController(FseCapabilityCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'CANAL')")
    @Operation(summary = "Lister les capacités FSE (providers avec codeFse)")
    public List<FseCapabilityView> list() {
        return catalogService.listAll();
    }

    @GetMapping("/{codeFse}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONFIG_MANAGER', 'SUPPORT', 'CANAL')")
    @Operation(summary = "Capacités d'un FSE")
    public ResponseEntity<FseCapabilityView> get(@PathVariable String codeFse) {
        try {
            return ResponseEntity.ok(catalogService.getByCodeFse(codeFse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
