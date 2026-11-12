package com.gateway.payment.events;

import com.gateway.payment.common.crypto.SecretEncryptionService;
import com.gateway.payment.events.domain.DomainOutbox;
import com.gateway.payment.events.domain.WebhookSubscription;
import com.gateway.payment.events.repository.WebhookSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

/** Envoie un événement outbox aux subscriptions webhook du tenant (HMAC-SHA256). */
@Component
public class OutboundWebhookDispatcher {

    private static final Logger log = LoggerFactory.getLogger(OutboundWebhookDispatcher.class);

    private final WebhookSubscriptionRepository subscriptions;
    private final SecretEncryptionService encryption;
    private final GatewayEventsProperties properties;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public OutboundWebhookDispatcher(
            WebhookSubscriptionRepository subscriptions,
            SecretEncryptionService encryption,
            GatewayEventsProperties properties
    ) {
        this.subscriptions = subscriptions;
        this.encryption = encryption;
        this.properties = properties;
    }

    public void dispatch(DomainOutbox event) throws Exception {
        if (!properties.outboundWebhooksEnabled()) {
            return;
        }
        List<WebhookSubscription> targets;
        if (event.getTenantId() != null && !event.getTenantId().isBlank()) {
            targets = subscriptions.findByTenantIdAndActiveTrue(event.getTenantId());
        } else {
            targets = List.of();
        }
        for (WebhookSubscription sub : targets) {
            if (!sub.matches(event.getEventType())) {
                continue;
            }
            post(sub, event);
        }
    }

    private void post(WebhookSubscription sub, DomainOutbox event) throws Exception {
        String body = event.getPayloadJson();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(sub.getTargetUrl()))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-Event-Id", event.getEventId())
                .header("X-Event-Type", event.getEventType())
                .header("X-Aggregate-Id", event.getAggregateId())
                .POST(HttpRequest.BodyPublishers.ofString(body));

        if (sub.getSecretEnc() != null && !sub.getSecretEnc().isBlank()) {
            String secret = encryption.decrypt(sub.getSecretEnc());
            builder.header("X-Signature", hmacSha256(secret, body));
        }

        HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Webhook " + sub.getCode() + " HTTP " + response.statusCode());
        }
        log.debug("Webhook sortant {} → {} OK", sub.getCode(), event.getEventType());
    }

    private static String hmacSha256(String secret, String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }
}
