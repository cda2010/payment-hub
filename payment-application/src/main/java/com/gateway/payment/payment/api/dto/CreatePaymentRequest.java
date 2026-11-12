package com.gateway.payment.payment.api.dto;

import com.gateway.payment.common.domain.ServiceTypeCode;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Création de paiement.
 * <ul>
 *   <li>Legacy : {@code provider} (+ {@code serviceType}) obligatoire.</li>
 *   <li>Catalogue (Option B) : {@code catalogServiceCode} ; {@code provider} optionnel
 *       si la version catalogue définit {@code defaultPayProvider} / {@code defaultProvider}.</li>
 * </ul>
 */
public record CreatePaymentRequest(
        String provider,
        /**
         * Type de service d'exécution (LINK, TRANSFERT, PAYEMENT, PAIEMENT_FACTURE…).
         * Optionnel si {@code catalogServiceCode} permet de le dériver.
         */
        ServiceTypeCode serviceType,
        /**
         * Produit catalogue (ex. BILL_PAYMENT, ACCOUNT_TRANSFER).
         * Si fourni, PaymentEngine résout la version PUBLISHED et fige le snapshot.
         */
        String catalogServiceCode,
        /** Version catalogue précise (sinon version courante PUBLISHED du code). */
        UUID catalogServiceVersionId,
        @DecimalMin("0") BigDecimal amount,
        String currency,
        String customerReference,
        String merchantReference,
        String phoneNumber,
        Map<String, Object> data
) {
    public CreatePaymentRequest {
        if (currency == null || currency.isBlank()) {
            currency = "XOF";
        }
    }
}
