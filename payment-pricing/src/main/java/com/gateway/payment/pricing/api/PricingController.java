package com.gateway.payment.pricing.api;

import com.gateway.payment.pricing.api.dto.PricingDtos.*;
import com.gateway.payment.pricing.service.PricingEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pricing")
@Tag(name = "Pricing", description = "Moteur Frais · Taux (plafonds → payment-rules)")
public class PricingController {

    private final PricingEngineService pricing;

    public PricingController(PricingEngineService pricing) {
        this.pricing = pricing;
    }

    @PostMapping("/quote")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Calculer un devis (frais via les grilles tarifaires + taux)")
    public QuoteResponse quote(@RequestBody QuoteRequest body) {
        try {
            return pricing.quote(body);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/rates")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les taux (FX / marge)")
    public List<RateDto> rates() {
        return pricing.listRates();
    }

    @GetMapping("/policies")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les policies produit / canal")
    public List<PolicyDto> policies() {
        return pricing.listPolicies();
    }
}
