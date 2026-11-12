package com.gateway.payment.payment.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.config.GatewayPerformanceProperties;
import com.gateway.payment.common.util.SensitiveDataMasker;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.observability.metrics.GatewayMetrics;
import com.gateway.payment.observability.resilience.ProviderCircuitBreaker;
import com.gateway.payment.observability.resilience.ProviderInfrastructureException;
import com.gateway.payment.payment.domain.PaymentExecution;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentExecutionRepository;
import com.gateway.payment.provider.ProviderExecutionResult;
import com.gateway.payment.provider.ProviderInvocationPort;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Journal {@link PaymentExecution} + circuit breaker Resilience4j.
 * Appels fournisseurs via {@link ConnectorHttpInvoker} (connecteurs HTTP séparés).
 */
@Service
public class ProviderInvocationService implements ProviderInvocationPort {

    private static final Logger log = LoggerFactory.getLogger(ProviderInvocationService.class);

    private final PaymentExecutionRepository executionRepository;
    private final PaymentProviderRepository providerRepository;
    private final GatewayMetrics gatewayMetrics;
    private final ProviderCircuitBreaker circuitBreaker;
    private final ObjectMapper objectMapper;
    private final PaymentExecutionAuditWriter auditWriter;
    private final GatewayPerformanceProperties performanceProperties;
    private final ConnectorHttpInvoker connectorHttpInvoker;

    public ProviderInvocationService(
            PaymentExecutionRepository executionRepository,
            PaymentProviderRepository providerRepository,
            GatewayMetrics gatewayMetrics,
            ProviderCircuitBreaker circuitBreaker,
            ObjectMapper objectMapper,
            PaymentExecutionAuditWriter auditWriter,
            GatewayPerformanceProperties performanceProperties,
            ConnectorHttpInvoker connectorHttpInvoker
    ) {
        this.executionRepository = executionRepository;
        this.providerRepository = providerRepository;
        this.gatewayMetrics = gatewayMetrics;
        this.circuitBreaker = circuitBreaker;
        this.objectMapper = objectMapper;
        this.auditWriter = auditWriter;
        this.performanceProperties = performanceProperties;
        this.connectorHttpInvoker = connectorHttpInvoker;
    }

    public ProviderExecutionResult call(
            PaymentTransaction tx,
            String stepCode,
            String operationCode,
            Map<String, Object> input,
            Map<String, Object> contextAttributes
    ) {
        return callWithProvider(tx, tx.getProviderCode(), stepCode, operationCode, input, contextAttributes);
    }

    /**
     * Appel fournisseur pour une jambe d'orchestration multi-FSE (provider ≠ tx.providerCode).
     * Ne mute pas {@code tx} — safe en jambes parallèles.
     */
    public ProviderExecutionResult callForProvider(
            PaymentTransaction tx,
            String providerCode,
            String stepCode,
            String operationCode,
            Map<String, Object> input,
            Map<String, Object> contextAttributes
    ) {
        return callWithProvider(tx, providerCode, stepCode, operationCode, input, contextAttributes);
    }

    private ProviderExecutionResult callWithProvider(
            PaymentTransaction tx,
            String providerCode,
            String stepCode,
            String operationCode,
            Map<String, Object> input,
            Map<String, Object> contextAttributes
    ) {
        Instant start = Instant.now();

        if (!circuitBreaker.isEnabled()) {
            ProviderExecutionResult result = invokeProvider(tx, providerCode, stepCode, operationCode, input, contextAttributes);
            persistAndMetric(tx, stepCode, operationCode, input, contextAttributes, result, start);
            return result;
        }

        if (circuitBreaker.isOpen(providerCode, operationCode)) {
            ProviderExecutionResult blocked = openCircuitResult(providerCode, operationCode);
            persistAndMetric(tx, stepCode, operationCode, input, contextAttributes, blocked, start);
            return blocked;
        }

        CircuitBreaker cb = circuitBreaker.of(providerCode, operationCode);
        ProviderExecutionResult result;
        try {
            result = cb.executeSupplier(() -> {
                ProviderExecutionResult executed = invokeProvider(
                        tx, providerCode, stepCode, operationCode, input, contextAttributes
                );
                // Comme Omnicanal : les échecs infra ouvrent le circuit via exception Resilience4j
                if (ProviderCircuitBreaker.isInfrastructureFailure(executed)) {
                    throw new ProviderInfrastructureException(
                            providerCode,
                            executed.getProviderMessage() != null
                                    ? executed.getProviderMessage()
                                    : "Provider infra failure on " + operationCode
                    );
                }
                return executed;
            });
        } catch (CallNotPermittedException ex) {
            log.warn("Circuit OPEN — skip {} / {}", providerCode, operationCode);
            result = openCircuitResult(providerCode, operationCode);
        } catch (ProviderInfrastructureException ex) {
            log.error("Provider infra error on {} / {}: {}", providerCode, operationCode, ex.getMessage());
            String msg = ex.getMessage() != null ? ex.getMessage() : "Erreur infrastructure fournisseur";
            boolean timedOut = msg.toLowerCase().contains("timeout")
                    || msg.toLowerCase().contains("indisponib");
            result = ProviderExecutionResult.builder()
                    .success(false)
                    .timedOut(timedOut)
                    .gatewayStatus(PaymentStatus.UNKNOWN)
                    .providerMessage(msg)
                    .httpStatus(timedOut ? 504 : 503)
                    .build();
        }

        persistAndMetric(tx, stepCode, operationCode, input, contextAttributes, result, start);
        return result;
    }

    private ProviderExecutionResult invokeProvider(
            PaymentTransaction tx,
            String providerCode,
            String stepCode,
            String operationCode,
            Map<String, Object> input,
            Map<String, Object> contextAttributes
    ) {
        log.debug("HTTP connector {} / {} / {} paymentId={}",
                providerCode, stepCode, operationCode, tx.getPaymentId());
        return connectorHttpInvoker.invoke(tx, providerCode, operationCode, input, contextAttributes);
    }

    private void persistAndMetric(
            PaymentTransaction tx,
            String stepCode,
            String operationCode,
            Map<String, Object> input,
            Map<String, Object> contextAttributes,
            ProviderExecutionResult result,
            Instant start
    ) {
        Instant end = Instant.now();
        long duration = result.getDurationMs() != null
                ? result.getDurationMs()
                : (end.toEpochMilli() - start.toEpochMilli());

        Map<String, Object> context = mergeContext(input, contextAttributes);
        PaymentExecution execution = PaymentExecution.builder()
                .paymentId(tx.getPaymentId())
                .stepCode(stepCode)
                .operationCode(operationCode)
                .providerCode(tx.getProviderCode())
                .requestTimestamp(start)
                .responseTimestamp(end)
                .httpStatus(result.getHttpStatus())
                .requestPayload(SensitiveDataMasker.maskJson(writeJson(sanitize(context))))
                .responsePayload(SensitiveDataMasker.maskJson(result.getRawResponse()))
                .providerCodeStatus(result.getProviderStatus())
                .providerMessage(result.getProviderMessage())
                .gatewayStatus(result.getGatewayStatus() != null ? result.getGatewayStatus().name() : null)
                .executionDurationMs(duration)
                .success(result.isSuccess())
                .build();

        if (performanceProperties.isAsyncAudit()) {
            auditWriter.persistAsync(execution);
        } else {
            executionRepository.save(execution);
        }

        recordMetrics(tx, operationCode, input, result, duration);
    }

    private static ProviderExecutionResult openCircuitResult(String providerCode, String operationCode) {
        return ProviderExecutionResult.builder()
                .success(false)
                .gatewayStatus(PaymentStatus.FAILED)
                .providerMessage("Circuit breaker OPEN for " + providerCode + " / " + operationCode)
                .providerStatus("CIRCUIT_OPEN")
                .httpStatus(503)
                .build();
    }

    private static Map<String, Object> mergeContext(
            Map<String, Object> input,
            Map<String, Object> contextAttributes
    ) {
        Map<String, Object> context = new HashMap<>();
        if (contextAttributes != null) {
            context.putAll(contextAttributes);
        }
        if (input != null) {
            context.putAll(input);
        }
        return context;
    }

    private void recordMetrics(
            PaymentTransaction tx,
            String operationCode,
            Map<String, Object> input,
            ProviderExecutionResult result,
            long durationMs
    ) {
        String codeFse = providerRepository.findByCode(tx.getProviderCode())
                .map(PaymentProvider::getCodeFse)
                .orElse(null);
        boolean infra = ProviderCircuitBreaker.isInfrastructureFailure(result);
        gatewayMetrics.recordProviderCall(tx.getProviderCode(), codeFse, operationCode,
                result.isSuccess(), infra, durationMs);
        gatewayMetrics.recordBusinessOperation(tx.getProviderCode(), codeFse, operationCode,
                result, input != null ? input : Map.of(), durationMs);
    }

    private Map<String, Object> sanitize(Map<String, Object> source) {
        Map<String, Object> copy = new HashMap<>(source);
        copy.remove("otp");
        return copy;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }
}
