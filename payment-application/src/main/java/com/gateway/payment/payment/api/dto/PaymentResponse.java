package com.gateway.payment.payment.api.dto;

import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.domain.ServiceTypeCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private String paymentId;
    private String provider;
    private ServiceTypeCode serviceType;
    /** Code produit catalogue figé (ex. BILL_PAYMENT), si initié via Option B. */
    private String catalogServiceCode;
    private UUID catalogServiceVersionId;
    private String catalogServiceVersion;
    /** Journey catalogue (ex. STANDARD_CASH_OUT). */
    private String journeyCode;
    /** Canal d'accès (MOBILE, GAB, USSD, BACKOFFICE). */
    private String accessChannel;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String currentStep;
    private String providerReference;
    private String customerReference;
    private String phoneNumber;
    private NextActionDto nextAction;
    private String errorCode;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;
    /** Libellé relevé formaté (Accept-Language) — listes supervision / historique. */
    private String title;
    private String subtitle;
    private String productLabel;
    private String statusLabel;
    /** Point de retrait / dépôt nommé (agent, agence) — détail opération. */
    private String agentDisplay;
    /** Canal de retrait (GAB / AGENT / AGENCY). */
    private String withdrawalChannel;
}
