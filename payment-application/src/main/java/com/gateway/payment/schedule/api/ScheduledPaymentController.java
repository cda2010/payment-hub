package com.gateway.payment.schedule.api;

import com.gateway.payment.schedule.domain.ScheduledPayment;
import com.gateway.payment.schedule.service.ScheduledPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scheduled-payments")
@Tag(name = "Scheduled payments", description = "Paiements programmés")
public class ScheduledPaymentController {

    private final ScheduledPaymentService service;

    public ScheduledPaymentController(ScheduledPaymentService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les paiements programmés actifs")
    public List<Map<String, Object>> list() {
        return service.listActive().stream().map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Détail d'un paiement programmé")
    public Map<String, Object> get(@PathVariable UUID id) {
        try {
            return toDto(service.get(id));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Créer un paiement programmé")
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        try {
            ScheduledPayment draft = ScheduledPayment.builder()
                    .label(str(body.get("label")))
                    .productCode(str(body.get("productCode")))
                    .amount(decimal(body.get("amount")))
                    .currency(str(body.get("currency")))
                    .frequency(ScheduledPaymentService.parseFrequency(str(body.get("frequency"))))
                    .nextRunAt(date(body.get("nextRunAt")))
                    .destinationProvider(str(body.get("destinationProvider")))
                    .destPhone(str(body.get("destPhone")))
                    .destAccount(str(body.get("destAccount")))
                    .beneficiaryLabel(str(body.get("beneficiaryLabel")))
                    .billerCode(str(body.get("billerCode")))
                    .subscriberReference(str(body.get("subscriberReference")))
                    .sourceRail(str(body.get("sourceRail")))
                    .sourcePaymentOrderId(str(body.get("sourcePaymentOrderId")))
                    .payloadJson(str(body.get("payloadJson")))
                    .ownerKey(str(body.get("ownerKey")))
                    .build();
            if (!org.springframework.util.StringUtils.hasText(draft.getLabel())) {
                draft.setLabel(defaultLabel(draft));
            }
            return toDto(service.create(draft));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/pause")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Suspendre un paiement programmé")
    public Map<String, Object> pause(@PathVariable UUID id) {
        try {
            return toDto(service.pause(id));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/resume")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reprendre un paiement programmé")
    public Map<String, Object> resume(@PathVariable UUID id) {
        try {
            return toDto(service.resume(id));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Supprimer un paiement programmé")
    public void delete(@PathVariable UUID id) {
        try {
            service.delete(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private Map<String, Object> toDto(ScheduledPayment sp) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", sp.getId() != null ? sp.getId().toString() : null);
        m.put("label", sp.getLabel());
        m.put("productCode", sp.getProductCode());
        m.put("amount", sp.getAmount());
        m.put("currency", sp.getCurrency());
        m.put("frequency", sp.getFrequency() != null ? sp.getFrequency().name() : null);
        m.put("nextRunAt", sp.getNextRunAt() != null ? sp.getNextRunAt().toString() : null);
        m.put("status", sp.getStatus() != null ? sp.getStatus().name() : null);
        m.put("destinationProvider", sp.getDestinationProvider());
        m.put("destPhone", sp.getDestPhone());
        m.put("destAccount", sp.getDestAccount());
        m.put("beneficiaryLabel", sp.getBeneficiaryLabel());
        m.put("billerCode", sp.getBillerCode());
        m.put("subscriberReference", sp.getSubscriberReference());
        m.put("sourceRail", sp.getSourceRail());
        m.put("sourcePaymentOrderId", sp.getSourcePaymentOrderId());
        m.put("actif", sp.isActif());
        return m;
    }

    private static String defaultLabel(ScheduledPayment draft) {
        if (org.springframework.util.StringUtils.hasText(draft.getBeneficiaryLabel())) {
            return draft.getBeneficiaryLabel();
        }
        if (org.springframework.util.StringUtils.hasText(draft.getBillerCode())) {
            return draft.getBillerCode();
        }
        return draft.getProductCode();
    }

    private static String str(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return s.isEmpty() || "null".equals(s) ? null : s;
    }

    private static BigDecimal decimal(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(String.valueOf(v).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDate date(Object v) {
        String s = str(v);
        if (s == null) return null;
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }
}
