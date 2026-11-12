package com.gateway.payment.observability.api;

import com.gateway.payment.observability.resilience.ProviderCircuitBreaker;
import com.gateway.payment.observability.resilience.ProviderResilienceProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Admin circuit breaker par <strong>méthode</strong> fournisseur.
 * <pre>
 * GET  /api/v1/admin/circuits/{providerCode}/{operationCode}
 * POST /api/v1/admin/circuits/{providerCode}/{operationCode}/force-open
 * POST /api/v1/admin/circuits/{providerCode}/{operationCode}/reset
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/admin/circuits")
@Tag(name = "Provider Circuit Breaker")
@SecurityRequirement(name = "bearerAuth")
public class ProviderCircuitController {

    private final ProviderCircuitBreaker circuitBreaker;
    private final ProviderResilienceProperties properties;

    public ProviderCircuitController(
            ProviderCircuitBreaker circuitBreaker,
            ProviderResilienceProperties properties
    ) {
        this.circuitBreaker = circuitBreaker;
        this.properties = properties;
    }

    @GetMapping("/{providerCode}/{operationCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'OPERATOR', 'CONFIG_MANAGER')")
    @Operation(summary = "État du circuit breaker d'une méthode fournisseur")
    public Map<String, Object> status(
            @PathVariable String providerCode,
            @PathVariable String operationCode
    ) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("providerCode", providerCode);
        out.put("operationCode", operationCode);
        out.put("enabled", properties.isEnabled());
        out.put("state", circuitBreaker.state(providerCode, operationCode));
        out.put("open", circuitBreaker.isOpen(providerCode, operationCode));
        out.put("circuitKey", ProviderCircuitBreaker.circuitKey(providerCode, operationCode));
        out.put("failureThreshold", properties.getFailureThreshold());
        out.put("failureRateThreshold", properties.getFailureRateThreshold());
        out.put("openDurationMs", properties.getOpenDurationMs());
        return out;
    }

    @PostMapping("/{providerCode}/{operationCode}/force-open")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'CONFIG_MANAGER')")
    @Operation(summary = "Force l'ouverture du circuit sur une méthode (break manuel)")
    public Map<String, Object> forceOpen(
            @PathVariable String providerCode,
            @PathVariable String operationCode
    ) {
        circuitBreaker.forceOpen(providerCode, operationCode);
        return status(providerCode, operationCode);
    }

    @PostMapping("/{providerCode}/{operationCode}/reset")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'CONFIG_MANAGER')")
    @Operation(summary = "Remet le circuit méthode à CLOSED")
    public Map<String, Object> reset(
            @PathVariable String providerCode,
            @PathVariable String operationCode
    ) {
        circuitBreaker.reset(providerCode, operationCode);
        return status(providerCode, operationCode);
    }
}
