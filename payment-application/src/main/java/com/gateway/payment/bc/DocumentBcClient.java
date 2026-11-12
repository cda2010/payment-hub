package com.gateway.payment.bc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Client HTTP payment-documents — coffre (métadonnées + binaire hors gateway).
 */
@Component
public class DocumentBcClient {

    private static final Logger log = LoggerFactory.getLogger(DocumentBcClient.class);

    private final RestClient http;
    private final GatewayBcProperties properties;

    public DocumentBcClient(RestClient.Builder builder, GatewayBcProperties properties) {
        this.properties = properties;
        Duration timeout = Duration.parse(properties.documents().timeout());
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.http = builder.baseUrl(properties.documents().url()).requestFactory(factory).build();
    }

    public boolean enabled() {
        return properties.documentsEnabled();
    }

    public Optional<StoredDocument> upload(
            byte[] content,
            String filename,
            String contentType,
            UploadCommand command
    ) {
        if (!enabled() || content == null || content.length == 0 || command == null) {
            return Optional.empty();
        }
        try {
            MultipartBodyBuilder body = new MultipartBodyBuilder();
            body.part("file", new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return StringUtils.hasText(filename) ? filename : "document.bin";
                }
            }).contentType(MediaType.parseMediaType(
                    StringUtils.hasText(contentType) ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE));
            body.part("meta", Map.of(
                    "tenantId", nullToEmpty(command.tenantId()),
                    "code", nullToEmpty(command.code()),
                    "ownerKind", nullToEmpty(command.ownerKind()),
                    "ownerId", nullToEmpty(command.ownerId()),
                    "paymentId", nullToEmpty(command.paymentId())
            )).contentType(MediaType.APPLICATION_JSON);

            Map<?, ?> response = http.post()
                    .uri("/api/v1/documents")
                    .headers(this::auth)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body.build())
                    .retrieve()
                    .body(Map.class);
            return Optional.ofNullable(parse(response));
        } catch (HttpStatusCodeException e) {
            log.warn("payment-documents upload → {}", e.getStatusCode().value());
            throw new IllegalStateException("payment-documents a refusé l'upload (" + e.getStatusCode().value() + ")");
        } catch (Exception e) {
            log.warn("payment-documents injoignable (upload): {}", e.getMessage());
            throw new IllegalStateException("payment-documents injoignable: " + e.getMessage(), e);
        }
    }

    public List<StoredDocument> listByPayment(String paymentId) {
        if (!enabled() || !StringUtils.hasText(paymentId)) {
            return List.of();
        }
        try {
            List<?> body = http.get()
                    .uri(u -> u.path("/api/v1/documents").queryParam("paymentId", paymentId.trim()).build())
                    .headers(this::auth)
                    .retrieve()
                    .body(List.class);
            return parseList(body);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return List.of();
            }
            log.warn("payment-documents list payment={} → {}", paymentId, e.getStatusCode().value());
            throw new IllegalStateException("payment-documents a refusé la lecture (" + e.getStatusCode().value() + ")");
        } catch (Exception e) {
            log.warn("payment-documents injoignable (list): {}", e.getMessage());
            throw new IllegalStateException("payment-documents injoignable: " + e.getMessage(), e);
        }
    }

    public Optional<StoredDocument> get(UUID id) {
        if (!enabled() || id == null) {
            return Optional.empty();
        }
        try {
            Map<?, ?> body = http.get()
                    .uri("/api/v1/documents/{id}", id)
                    .headers(this::auth)
                    .retrieve()
                    .body(Map.class);
            return Optional.ofNullable(parse(body));
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return Optional.empty();
            }
            log.warn("payment-documents get {} → {}", id, e.getStatusCode().value());
            throw new IllegalStateException("payment-documents a refusé la lecture (" + e.getStatusCode().value() + ")");
        } catch (Exception e) {
            log.warn("payment-documents injoignable (get): {}", e.getMessage());
            throw new IllegalStateException("payment-documents injoignable: " + e.getMessage(), e);
        }
    }

    private void auth(HttpHeaders headers) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            String token = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (token != null && !token.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, token);
            }
        }
    }

    private static List<StoredDocument> parseList(List<?> body) {
        if (body == null || body.isEmpty()) {
            return List.of();
        }
        List<StoredDocument> out = new ArrayList<>();
        for (Object row : body) {
            StoredDocument doc = parse(row);
            if (doc != null) {
                out.add(doc);
            }
        }
        return out;
    }

    private static StoredDocument parse(Object o) {
        if (!(o instanceof Map<?, ?> m)) {
            return null;
        }
        UUID id = null;
        Object rawId = m.get("id");
        if (rawId != null) {
            try {
                id = UUID.fromString(String.valueOf(rawId));
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        if (id == null) {
            return null;
        }
        long size = 0L;
        Object rawSize = m.get("sizeBytes");
        if (rawSize instanceof Number n) {
            size = n.longValue();
        }
        return new StoredDocument(
                id,
                str(m.get("tenantId")),
                str(m.get("code")),
                str(m.get("ownerKind")),
                str(m.get("ownerId")),
                str(m.get("paymentId")),
                str(m.get("originalName")),
                str(m.get("contentType")),
                size,
                str(m.get("checksumSha256")),
                str(m.get("status")),
                str(m.get("downloadUrl"))
        );
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    public record UploadCommand(
            String tenantId,
            String code,
            String ownerKind,
            String ownerId,
            String paymentId
    ) {
    }

    public record StoredDocument(
            UUID id,
            String tenantId,
            String code,
            String ownerKind,
            String ownerId,
            String paymentId,
            String originalName,
            String contentType,
            long sizeBytes,
            String checksumSha256,
            String status,
            String downloadUrl
    ) {
    }
}
