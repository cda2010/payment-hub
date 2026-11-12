package com.gateway.payment.observability.api;

import com.gateway.payment.observability.catalog.ParticipantCatalogService;
import com.gateway.payment.observability.service.OpsMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/metrics")
@Tag(name = "Ops Metrics")
@SecurityRequirement(name = "bearerAuth")
public class OpsMetricsController {

    private final OpsMetricsService metricsService;

    public OpsMetricsController(OpsMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'OPERATOR', 'AUDITOR', 'CONFIG_MANAGER')")
    @Operation(summary = "Resume metriques gateway (Micrometer + executions du jour)")
    public Map<String, Object> summary() {
        return metricsService.summary();
    }

    @GetMapping("/prometheus-hints")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'CONFIG_MANAGER')")
    @Operation(summary = "Points d'acces Prometheus / Actuator")
    public Map<String, Object> prometheusHints() {
        return metricsService.prometheusHints();
    }
}
