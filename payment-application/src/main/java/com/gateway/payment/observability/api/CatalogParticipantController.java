package com.gateway.payment.observability.api;

import com.gateway.payment.observability.catalog.ParticipantCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Catalogue participants FSE — compatible Omnicanal {@code /api/v1/catalog/participants}.
 */
@RestController
@RequestMapping("/api/v1/catalog")
@Tag(name = "Participant Catalog")
public class CatalogParticipantController {

    private final ParticipantCatalogService catalogService;

    public CatalogParticipantController(ParticipantCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/participants")
    @Operation(summary = "Lister les participants FSE")
    public List<Map<String, Object>> participants() {
        return catalogService.listParticipants();
    }

    @GetMapping("/participants/{participantCode}")
    @Operation(summary = "Detail participant FSE")
    public Map<String, Object> one(@PathVariable String participantCode) {
        return catalogService.getParticipant(participantCode);
    }

    @GetMapping("/participants/{participantCode}/capabilities")
    @Operation(summary = "Capacites du participant")
    public List<Map<String, Object>> capabilities(@PathVariable String participantCode) {
        return catalogService.listCapabilities(participantCode);
    }

    @GetMapping("/participants/{participantCode}/products/{productCode}")
    @Operation(summary = "Support produit par participant")
    public Map<String, Object> productSupport(
            @PathVariable String participantCode,
            @PathVariable String productCode
    ) {
        return catalogService.productSupport(participantCode, productCode);
    }
}
