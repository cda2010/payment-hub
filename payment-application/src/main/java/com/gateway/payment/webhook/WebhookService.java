package com.gateway.payment.webhook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.common.crypto.SecretEncryptionService;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.domain.ProviderWebhookConfig;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.repository.ProviderWebhookConfigRepository;
import com.gateway.payment.events.GatewayEventsProperties;
import com.gateway.payment.payment.application.PaymentEngine;
import com.gateway.payment.payment.domain.PaymentCallback;
import com.gateway.payment.payment.repository.PaymentCallbackRepository;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

@Service
public class WebhookService {

    private final ProviderWebhookConfigRepository webhookConfigRepository;
    private final PaymentProviderRepository providerRepository;
    private final PaymentCallbackRepository callbackRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentEngine paymentEngine;
    private final SecretEncryptionService encryptionService;
    private final ObjectMapper objectMapper;
    private final GatewayEventsProperties eventsProperties;

    public WebhookService(
            ProviderWebhookConfigRepository webhookConfigRepository,
            PaymentProviderRepository providerRepository,
            PaymentCallbackRepository callbackRepository,
            PaymentTransactionRepository transactionRepository,
            PaymentEngine paymentEngine,
            SecretEncryptionService encryptionService,
            ObjectMapper objectMapper,
            GatewayEventsProperties eventsProperties
    ) {
        this.webhookConfigRepository = webhookConfigRepository;
        this.providerRepository = providerRepository;
        this.callbackRepository = callbackRepository;
        this.transactionRepository = transactionRepository;
        this.paymentEngine = paymentEngine;
        this.encryptionService = encryptionService;
        this.objectMapper = objectMapper;
        this.eventsProperties = eventsProperties;
    }

    @Transactional
    public Map<String, Object> handle(String providerPath, String rawBody, HttpServletRequest request) {
        ProviderWebhookConfig config = webhookConfigRepository.findByPathSuffix(providerPath)
                .or(() -> webhookConfigRepository.findByPathSuffix("/" + providerPath))
                .orElseThrow(() -> new BusinessException("WEBHOOK_UNKNOWN", "Webhook inconnu", HttpStatus.NOT_FOUND));

        if (!config.isActif()) {
            throw new BusinessException("WEBHOOK_DISABLED", "Webhook désactivé", HttpStatus.FORBIDDEN);
        }

        verify(config, rawBody, request);

        Map<String, Object> payload = parse(rawBody);
        String externalEventId = first(payload, "id", "eventId", "event_id", "transactionId");
        String paymentId = first(payload, "paymentId", "client_reference", "merchantReference", "clientReference");
        String providerRef = first(payload, "providerReference", "reference", "id", "transaction_id");
        String status = first(payload, "status", "code", "payment_status");

        if (externalEventId != null) {
            Optional<PaymentCallback> existing = callbackRepository
                    .findByProviderCodeAndExternalEventId(config.getProvider().getCode(), externalEventId);
            if (existing.isPresent() && existing.get().isProcessed()) {
                return Map.of("status", "ALREADY_PROCESSED");
            }
        }

        if (paymentId == null && providerRef != null) {
            paymentId = transactionRepository.findByProviderReference(providerRef)
                    .map(t -> t.getPaymentId())
                    .orElse(null);
        }

        PaymentCallback callback = PaymentCallback.builder()
                .providerCode(config.getProvider().getCode())
                .paymentId(paymentId)
                .externalEventId(externalEventId)
                .payload(rawBody)
                .processed(false)
                .build();
        callbackRepository.save(callback);

        if (paymentId != null) {
            paymentEngine.applyWebhookUpdate(paymentId, status, providerRef, "Webhook " + config.getProvider().getCode());
            callback.setProcessed(true);
            callback.setProcessedAt(Instant.now());
            callbackRepository.save(callback);
        }

        return Map.of("status", "OK", "paymentId", paymentId != null ? paymentId : "");
    }

    private void verify(ProviderWebhookConfig config, String rawBody, HttpServletRequest request) {
        String mode = config.getAuthMode() != null ? config.getAuthMode() : "NONE";
        if (eventsProperties.inboundRequireAuth()
                && ("NONE".equalsIgnoreCase(mode) || mode.isBlank())) {
            throw new BusinessException(
                    "WEBHOOK_AUTH_REQUIRED",
                    "Authentification webhook obligatoire (API_KEY, HMAC ou IP)",
                    HttpStatus.FORBIDDEN);
        }
        switch (mode.toUpperCase()) {
            case "API_KEY" -> {
                String expected = encryptionService.decrypt(config.getApiKeyEnc());
                String provided = request.getHeader("X-API-Key");
                if (provided == null || !provided.equals(expected)) {
                    throw new BusinessException("WEBHOOK_AUTH_FAILED", "API Key invalide", HttpStatus.UNAUTHORIZED);
                }
            }
            case "HMAC" -> {
                String secret = encryptionService.decrypt(config.getHmacSecretEnc());
                String signature = request.getHeader("X-Signature");
                if (signature == null || !signature.equalsIgnoreCase(hmacSha256(secret, rawBody))) {
                    throw new BusinessException("WEBHOOK_AUTH_FAILED", "Signature HMAC invalide", HttpStatus.UNAUTHORIZED);
                }
            }
            case "IP" -> {
                if (config.getAllowedIps() != null && !config.getAllowedIps().isBlank()) {
                    String ip = request.getRemoteAddr();
                    boolean ok = java.util.Arrays.stream(config.getAllowedIps().split(","))
                            .map(String::trim)
                            .anyMatch(ip::equals);
                    if (!ok) {
                        throw new BusinessException("WEBHOOK_AUTH_FAILED", "IP non autorisée", HttpStatus.FORBIDDEN);
                    }
                } else if (eventsProperties.inboundRequireAuth()) {
                    throw new BusinessException("WEBHOOK_AUTH_FAILED", "Liste IP manquante", HttpStatus.FORBIDDEN);
                }
            }
            default -> {
                // NONE autorisé seulement si inbound-require-auth=false
            }
        }
    }

    private String hmacSha256(String secret, String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BusinessException("WEBHOOK_AUTH_FAILED", "Erreur signature", HttpStatus.UNAUTHORIZED);
        }
    }

    private Map<String, Object> parse(String raw) {
        try {
            return objectMapper.readValue(raw, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String first(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.get(key) != null) {
                return String.valueOf(map.get(key));
            }
        }
        return null;
    }
}
