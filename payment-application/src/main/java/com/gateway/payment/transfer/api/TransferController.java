package com.gateway.payment.transfer.api;

import com.gateway.payment.transfer.api.dto.TransferEstimateRequest;
import com.gateway.payment.transfer.api.dto.TransferModeOptionResponse;
import com.gateway.payment.transfer.api.dto.TransferParticipantResponse;
import com.gateway.payment.transfer.api.dto.TransferRailResponse;
import com.gateway.payment.transfer.service.TransferCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transfers")
@Tag(name = "Transfers")
@SecurityRequirement(name = "bearerAuth")
public class TransferController {

    private final TransferCatalogService catalogService;

    public TransferController(TransferCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/providers")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les fournisseurs actifs (émetteurs / bénéficiaires)")
    public List<TransferParticipantResponse> providers(
            @RequestParam(required = false) Boolean canEmit,
            @RequestParam(required = false) Boolean canReceive
    ) {
        return catalogService.listParticipants(true).stream()
                .filter(p -> canEmit == null || p.canEmit() == canEmit)
                .filter(p -> canReceive == null || p.canReceive() == canReceive)
                .toList();
    }

    @GetMapping("/participants")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Alias de /providers")
    public List<TransferParticipantResponse> participants() {
        return catalogService.listParticipants(true);
    }

    @GetMapping("/rails")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les rails de transfert actifs")
    public List<TransferRailResponse> rails() {
        return catalogService.listRails(true);
    }

    @GetMapping("/rails/eligible")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Rails éligibles pour une paire émetteur / bénéficiaire (+ montant optionnel)")
    public List<TransferRailResponse> eligibleRails(
            @RequestParam String emitterProviderCode,
            @RequestParam String beneficiaryProviderCode,
            @RequestParam(required = false) BigDecimal amount
    ) {
        return catalogService.listEligibleRails(emitterProviderCode, beneficiaryProviderCode, amount);
    }

    @PostMapping("/estimate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Comparer les modes d'envoi éligibles (frais + ETA, sans créer de commande)")
    public List<TransferModeOptionResponse> estimate(@Valid @RequestBody TransferEstimateRequest request) {
        return catalogService.estimateModes(request);
    }
}
