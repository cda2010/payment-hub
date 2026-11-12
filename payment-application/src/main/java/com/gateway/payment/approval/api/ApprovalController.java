package com.gateway.payment.approval.api;

import com.gateway.payment.approval.api.dto.ApprovalDtos;
import com.gateway.payment.approval.service.ApprovalQueryService;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.api.dto.PaymentActionRequest;
import com.gateway.payment.payment.api.dto.PaymentResponse;
import com.gateway.payment.payment.application.PaymentEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/approvals")
@Tag(name = "Approvals")
@SecurityRequirement(name = "bearerAuth")
public class ApprovalController {

    private final ApprovalQueryService queryService;
    private final PaymentEngine paymentEngine;

    public ApprovalController(ApprovalQueryService queryService, PaymentEngine paymentEngine) {
        this.queryService = queryService;
        this.paymentEngine = paymentEngine;
    }

    @GetMapping("/inbox")
    @Operation(summary = "Inbox des validations en attente (filtrée par rôles)")
    @PreAuthorize("isAuthenticated()")
    public List<ApprovalDtos.InboxItem> inbox() {
        return queryService.inbox();
    }

    @GetMapping("/by-payment/{paymentId}")
    @Operation(summary = "Détail du circuit d'approbation d'un paiement")
    @PreAuthorize("isAuthenticated()")
    public ApprovalDtos.Detail byPayment(@PathVariable String paymentId) {
        return queryService.detailByPaymentId(paymentId);
    }

    @GetMapping("/instances/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApprovalDtos.Detail byInstance(@PathVariable UUID id) {
        return queryService.detailByInstanceId(id);
    }

    @PostMapping("/{paymentId}/decide")
    @Operation(summary = "Approuver ou refuser — body.action = APPROVE|REJECT")
    @PreAuthorize("isAuthenticated()")
    public PaymentResponse decide(
            @PathVariable String paymentId,
            @Valid @RequestBody PaymentActionRequest request
    ) {
        String action = request.action() != null ? request.action().trim().toUpperCase() : "";
        if (!"APPROVE".equals(action) && !"REJECT".equals(action)) {
            throw new BusinessException("INVALID_ACTION", "action doit être APPROVE ou REJECT");
        }
        Map<String, Object> data = request.data() != null ? request.data() : Map.of();
        return paymentEngine.submitAction(paymentId, action, data);
    }
}
