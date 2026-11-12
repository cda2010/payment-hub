package com.gateway.payment.observability.metrics;

import com.gateway.payment.observability.resilience.ProviderCircuitBreaker;
import com.gateway.payment.provider.ProviderExecutionResult;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Observabilité gateway — maille primaire : operation × fse/provider × résultat.
 */
@Component
public class GatewayMetrics {

    private final MeterRegistry registry;
    private final BusinessOperationClassifier classifier;
    private final Set<String> boundCircuits = ConcurrentHashMap.newKeySet();

    public GatewayMetrics(MeterRegistry registry, BusinessOperationClassifier classifier) {
        this.registry = registry;
        this.classifier = classifier;
    }

    public void recordChannelRequest(String channel, String route, int status, long durationMs) {
        String ch = channel != null ? channel : "anonymous";
        String rt = route != null ? route : "unknown";
        String outcome = status >= 500 ? "error" : (status >= 400 ? "client_error" : "success");
        Timer.builder("payment.gateway.channel.latency")
                .description("Latence requetes API /api/v1")
                .tags(Tags.of("channel", ch, "route", rt, "status", String.valueOf(status), "outcome", outcome))
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
        registry.counter("payment.gateway.channel.requests",
                "channel", ch, "route", rt, "status", String.valueOf(status), "outcome", outcome
        ).increment();
    }

    public void recordProviderCall(
            String providerCode,
            String codeFse,
            String operationCode,
            boolean success,
            boolean infraError,
            long durationMs
    ) {
        String fse = StringUtils.hasText(codeFse) ? codeFse : providerCode;
        String method = operationCode != null ? operationCode : "unknown";
        String result = success ? "success" : "failure";
        String outcome = infraError ? "infra_error" : (success ? "success" : "business_error");
        Timer.builder("payment.gateway.provider.latency")
                .description("Latence appels fournisseur sortants")
                .tags(Tags.of("provider", providerCode, "fse", fse, "operation", method, "result", result, "outcome", outcome))
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
        registry.counter("payment.gateway.provider.calls",
                "provider", providerCode, "fse", fse, "operation", method, "result", result, "outcome", outcome
        ).increment();
    }

    public void recordBusinessOperation(
            String providerCode,
            String codeFse,
            String operationCode,
            ProviderExecutionResult result,
            Map<String, Object> params,
            long durationMs
    ) {
        String operation = classifier.operationOf(operationCode, operationCode);
        String category = classifier.categoryOf(operation);
        String fse = StringUtils.hasText(codeFse) ? codeFse : providerCode;

        boolean infra = result != null && ProviderCircuitBreaker.isInfrastructureFailure(result);
        boolean success = result != null && result.isSuccess();
        String resultLabel = success ? "success" : "failure";
        String outcome = infra ? "infra_error" : (success ? "success" : "business_error");
        String errCode = result != null && result.getGatewayStatus() != null
                ? result.getGatewayStatus().name()
                : "none";

        Tags withCategory = Tags.of(
                "operation", operation,
                "provider", providerCode,
                "fse", fse,
                "result", resultLabel,
                "outcome", outcome,
                "err_code", errCode,
                "category", category
        );

        registry.counter("payment.business.operation.calls", withCategory).increment();
        Timer.builder("payment.business.operation.latency")
                .description("Latence par operation x FSE")
                .tags(withCategory)
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);

        if (classifier.tracksAmount(operation)) {
            BusinessAmountExtractor.extractTransferAmount(params, result != null ? result.getResponseBody() : Map.of())
                    .ifPresent(value -> recordAmount("payment.business.operation.amount",
                            "Montant par operation", withCategory, value));
        }
        if (classifier.tracksBalance(operation) && success) {
            BusinessAmountExtractor.extractBalance(result != null ? result.getResponseBody() : Map.of())
                    .ifPresent(value -> recordAmount("payment.business.operation.balance",
                            "Solde observe", withCategory, value));
        }
    }

    public void bindCircuitBreaker(CircuitBreaker circuitBreaker) {
        if (circuitBreaker == null) {
            return;
        }
        String name = circuitBreaker.getName();
        if (!boundCircuits.add(name)) {
            return;
        }
        registry.gauge(
                "payment.gateway.provider.circuit.state",
                Tags.of("circuit", name),
                circuitBreaker,
                cb -> cb.getState().getOrder()
        );
    }

    private void recordAmount(String metricName, String description, Tags tags, BigDecimal value) {
        if (value == null) {
            return;
        }
        double amount = value.doubleValue();
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            return;
        }
        DistributionSummary.builder(metricName)
                .description(description)
                .baseUnit("currency")
                .tags(tags)
                .register(registry)
                .record(Math.abs(amount));
    }
}
