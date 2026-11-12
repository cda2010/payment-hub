package com.gateway.payment.billing.domain;

/**
 * Mode d'enquête facture dans le BPMN {@code providerBillPay}
 * ({@code billInquiryMode} Flowable).
 */
public final class BillInquiryMode {

    /** Mode 1 — {@link JourneyCode#OPEN_BILLS}. */
    public static final String LIST = "LIST";
    /** Mode 2 — {@link JourneyCode#GET_INFOS}. */
    public static final String INFOS = "INFOS";
    /** Mode 3 — {@link JourneyCode#PREPAID}. */
    public static final String DIRECT = "DIRECT";

    private BillInquiryMode() {}

    public static String fromJourney(JourneyCode journeyCode) {
        if (journeyCode == null) {
            return INFOS;
        }
        return switch (journeyCode) {
            case OPEN_BILLS -> LIST;
            case GET_INFOS -> INFOS;
            case PREPAID -> DIRECT;
        };
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String n = raw.trim().toUpperCase();
        return switch (n) {
            case LIST, "OPEN_BILLS", "BILL_LIST" -> LIST;
            case INFOS, "GET_INFOS", "GETINFOS", "LOOKUP", "LOOKUP_BILL" -> INFOS;
            case DIRECT, "PREPAID", "TOPUP" -> DIRECT;
            default -> n;
        };
    }
}
