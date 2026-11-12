package com.gateway.payment.payment.api;

import com.gateway.payment.payment.api.dto.PaymentResponse;
import com.gateway.payment.payment.application.PaymentQueryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/transactions")
@Tag(name = "Supervision")
@SecurityRequirement(name = "bearerAuth")
public class SupervisionController {

    private final PaymentQueryService paymentQueryService;

    public SupervisionController(PaymentQueryService paymentQueryService) {
        this.paymentQueryService = paymentQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPPORT','OPERATOR','AUDITOR','CONFIG_MANAGER')")
    public Page<PaymentResponse> search(
            @RequestParam(required = false) String paymentId,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerReference,
            @RequestParam(required = false) String providerReference,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable
    ) {
        return paymentQueryService.search(
                paymentId, provider, status, customerReference, providerReference, phoneNumber, from, to, pageable
        );
    }

    @GetMapping("/stats/daily")
    @PreAuthorize("hasAnyRole('ADMIN','SUPPORT','OPERATOR','AUDITOR','CONFIG_MANAGER')")
    public Map<String, Object> dailyStats() {
        return paymentQueryService.dailyStats();
    }
}
