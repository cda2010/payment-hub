package com.gateway.payment.events.api;

import com.gateway.payment.common.crypto.SecretEncryptionService;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.events.domain.WebhookSubscription;
import com.gateway.payment.events.repository.WebhookSubscriptionRepository;
import com.gateway.payment.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/webhook-subscriptions")
@Tag(name = "Webhook subscriptions", description = "Webhooks sortants partenaires (HMAC)")
@PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR')")
public class WebhookSubscriptionController {

    private final WebhookSubscriptionRepository repository;
    private final SecretEncryptionService encryption;

    public WebhookSubscriptionController(
            WebhookSubscriptionRepository repository,
            SecretEncryptionService encryption
    ) {
        this.repository = repository;
        this.encryption = encryption;
    }

    public record SubscriptionRequest(
            String tenantId,
            String code,
            String targetUrl,
            String secret,
            String eventTypes,
            Boolean active
    ) {}

    public record SubscriptionResponse(
            UUID id, String tenantId, String code, String targetUrl,
            String eventTypes, boolean active, boolean hasSecret
    ) {
        static SubscriptionResponse from(WebhookSubscription s) {
            return new SubscriptionResponse(
                    s.getId(), s.getTenantId(), s.getCode(), s.getTargetUrl(),
                    s.getEventTypes(), s.isActive(),
                    s.getSecretEnc() != null && !s.getSecretEnc().isBlank());
        }
    }

    @GetMapping
    @Operation(summary = "Lister les subscriptions (filtrées par tenant si non plateforme)")
    public List<SubscriptionResponse> list(@RequestParam(required = false) String tenantId) {
        String effective = resolveTenant(tenantId);
        List<WebhookSubscription> rows = effective == null
                ? repository.findByActiveTrue()
                : repository.findByTenantIdAndActiveTrue(effective);
        return rows.stream().map(SubscriptionResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse create(@RequestBody SubscriptionRequest body) {
        String tenant = resolveTenant(body.tenantId());
        if (tenant == null || tenant.isBlank()) {
            throw new BusinessException("TENANT_REQUIRED", "tenantId requis", HttpStatus.BAD_REQUEST);
        }
        if (body.code() == null || body.targetUrl() == null) {
            throw new BusinessException("INVALID", "code et targetUrl requis", HttpStatus.BAD_REQUEST);
        }
        WebhookSubscription sub = WebhookSubscription.builder()
                .tenantId(tenant)
                .code(body.code().trim())
                .targetUrl(body.targetUrl().trim())
                .secretEnc(body.secret() != null && !body.secret().isBlank()
                        ? encryption.encrypt(body.secret()) : null)
                .eventTypes(body.eventTypes() == null || body.eventTypes().isBlank() ? "*" : body.eventTypes())
                .active(body.active() == null || body.active())
                .build();
        return SubscriptionResponse.from(repository.save(sub));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        WebhookSubscription sub = repository.findById(id)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Subscription introuvable", HttpStatus.NOT_FOUND));
        if (!TenantContext.canAccess(sub.getTenantId())) {
            throw new BusinessException("FORBIDDEN", "Accès refusé", HttpStatus.FORBIDDEN);
        }
        sub.setActive(false);
        repository.save(sub);
    }

    private String resolveTenant(String requested) {
        if (TenantContext.isPlatformUser()) {
            return requested != null && !requested.isBlank() ? requested.trim() : null;
        }
        String current = TenantContext.currentTenantId();
        if (current == null) {
            return requested;
        }
        if (requested != null && !requested.isBlank() && !requested.equals(current)) {
            throw new BusinessException("FORBIDDEN", "Tenant non autorisé", HttpStatus.FORBIDDEN);
        }
        return current;
    }
}
