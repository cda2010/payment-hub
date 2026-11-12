package com.gateway.payment.events;

public final class DomainEventTypes {
    private DomainEventTypes() {}

    public static final String PAYMENT_SUCCEEDED = "payment.succeeded";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String PAYMENT_CANCELLED = "payment.cancelled";
    public static final String NOTIFICATION_REQUESTED = "notification.requested";

    /** Ouvre / rafraîchit une entrée inbox « À faire ». */
    public static final String PARTY_ACTION_OPEN = "party.action.open";
    /** Clôture une entrée inbox (DONE / DISMISSED / EXPIRED). */
    public static final String PARTY_ACTION_COMPLETE = "party.action.complete";
}
