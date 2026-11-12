package com.gateway.payment.payment.api;

import com.gateway.payment.payment.api.dto.CreatePaymentRequest;
import com.gateway.payment.payment.api.dto.PaymentActionRequest;
import com.gateway.payment.payment.api.dto.PaymentEventDto;
import com.gateway.payment.payment.api.dto.PaymentResponse;
import com.gateway.payment.payment.application.PaymentBpmViewService;
import com.gateway.payment.payment.application.PaymentEngine;
import com.gateway.payment.payment.application.PaymentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Produits runtime (CHECKOUT / BILL_PAYMENT / ACCOUNT_TRANSFER). "
        + "Contrat d'intégration : docs/INTEGRATION.md — boucle create → nextAction → actions.")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentEngine paymentEngine;
    private final PaymentQueryService paymentQueryService;
    private final PaymentBpmViewService paymentBpmViewService;

    public PaymentController(
            PaymentEngine paymentEngine,
            PaymentQueryService paymentQueryService,
            PaymentBpmViewService paymentBpmViewService
    ) {
        this.paymentEngine = paymentEngine;
        this.paymentQueryService = paymentQueryService;
        this.paymentBpmViewService = paymentBpmViewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Initialiser un paiement")
    public PaymentResponse create(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return paymentEngine.initiate(
                request.provider(),
                request.serviceType(),
                request.catalogServiceCode(),
                request.catalogServiceVersionId(),
                request.amount(),
                request.currency(),
                request.customerReference(),
                request.merchantReference(),
                request.phoneNumber(),
                request.data(),
                idempotencyKey
        );
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse get(@PathVariable String paymentId) {
        return paymentEngine.get(paymentId);
    }

    @GetMapping("/{paymentId}/status")
    public PaymentResponse status(@PathVariable String paymentId) {
        return paymentEngine.get(paymentId);
    }

    @PostMapping("/{paymentId}/actions")
    public PaymentResponse action(
            @PathVariable String paymentId,
            @Valid @RequestBody PaymentActionRequest request
    ) {
        return paymentEngine.submitAction(paymentId, request.action(), request.data());
    }

    @PostMapping("/{paymentId}/cancel")
    public PaymentResponse cancel(@PathVariable String paymentId) {
        return paymentEngine.cancel(paymentId);
    }

    @PostMapping("/{paymentId}/retry")
    public PaymentResponse retry(@PathVariable String paymentId) {
        return paymentEngine.retry(paymentId);
    }

    @GetMapping("/{paymentId}/events")
    public List<PaymentEventDto> events(@PathVariable String paymentId) {
        return paymentQueryService.timeline(paymentId);
    }

    @GetMapping("/{paymentId}/bpm/diagram-state")
    @Operation(summary = "État d'exécution Flowable (XML + activités) pour le viewer BPMN")
    public Map<String, Object> diagramState(@PathVariable String paymentId) {
        return paymentBpmViewService.diagramState(paymentId);
    }
}
