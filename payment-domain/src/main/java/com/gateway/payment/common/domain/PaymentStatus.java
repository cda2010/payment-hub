package com.gateway.payment.common.domain;

public enum PaymentStatus {
    CREATED,
    INITIATED,
    AWAITING_USER_INPUT,
    WAITING_APPROVAL,
    /** Approbation refusée — l'initiateur doit corriger et re-soumettre. */
    RETURNED_FOR_CORRECTION,
    OTP_GENERATION_PENDING,
    OTP_REQUIRED,
    PROCESSING,
    REDIRECT_REQUIRED,
    WAITING_CALLBACK,
    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED,
    EXPIRED,
    UNKNOWN
}
