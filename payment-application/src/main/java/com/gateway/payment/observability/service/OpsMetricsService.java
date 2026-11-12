package com.gateway.payment.observability.service;

import com.gateway.payment.observability.metrics.BusinessOperationClassifier;
import com.gateway.payment.payment.repository.PaymentExecutionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpsMetricsService {

    private final MeterRegistry meterRegistry;
    private final PaymentExecutionRepository executionRepository;
    private final BusinessOperationClassifier classifier;

    public OpsMetricsService(
            MeterRegistry meterRegistry,
            PaymentExecutionRepository executionRepository,
            BusinessOperationClassifier classifier
    ) {
        this.meterRegistry = meterRegistry;
        this.executionRepository = executionRepository;
        this.classifier = classifier;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> summary() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("channel", aggregateMeters("payment.gateway.channel.requests"));
        out.put("providerCalls", aggregateMeters("payment.gateway.provider.calls"));
        out.put("businessOperations", aggregateMeters("payment.business.operation.calls"));
        out.put("circuits", listCircuitGauges());
        out.put("classifier", classifier.snapshot());
        out.put("executionsToday", executionsToday());
        return out;
    }

    private List<Map<String, Object>> aggregateMeters(String name) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Meter meter : meterRegistry.find(name).meters()) {
            if (meter instanceof Counter counter) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("tags", meter.getId().getTags());
                row.put("count", counter.count());
                rows.add(row);
            }
        }
        return rows;
    }

    private List<Map<String, Object>> listCircuitGauges() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Meter meter : meterRegistry.find("payment.gateway.provider.circuit.state").gauges()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("tags", meter.getId().getTags());
            row.put("stateOrder", meter.measure().iterator().hasNext()
                    ? meter.measure().iterator().next().getValue()
                    : 0);
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Object>> executionsToday() {
        Instant start = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        return executionRepository.summarizeSince(start).stream()
                .map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("providerCode", row.getProviderCode());
                    m.put("operationCode", row.getOperationCode());
                    m.put("successCount", row.getSuccessCount());
                    m.put("failureCount", row.getFailureCount());
                    m.put("avgDurationMs", row.getAvgDurationMs());
                    return m;
                })
                .toList();
    }

    public Map<String, Object> prometheusHints() {
        Map<String, Object> hints = new HashMap<>();
        hints.put("endpoint", "/actuator/prometheus");
        hints.put("metricsEndpoint", "/actuator/metrics");
        hints.put("prefixes", List.of(
                "payment.gateway.channel",
                "payment.gateway.provider",
                "payment.business.operation"
        ));
        return hints;
    }
}
