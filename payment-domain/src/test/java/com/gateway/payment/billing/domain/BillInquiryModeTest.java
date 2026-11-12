package com.gateway.payment.billing.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BillInquiryModeTest {

    @Test
    void fromJourney_mapsThreeModes() {
        assertEquals(BillInquiryMode.LIST, BillInquiryMode.fromJourney(JourneyCode.OPEN_BILLS));
        assertEquals(BillInquiryMode.INFOS, BillInquiryMode.fromJourney(JourneyCode.GET_INFOS));
        assertEquals(BillInquiryMode.DIRECT, BillInquiryMode.fromJourney(JourneyCode.PREPAID));
        assertEquals(BillInquiryMode.INFOS, BillInquiryMode.fromJourney(null));
    }

    @Test
    void normalize_aliases() {
        assertEquals(BillInquiryMode.LIST, BillInquiryMode.normalize("open_bills"));
        assertEquals(BillInquiryMode.INFOS, BillInquiryMode.normalize("LOOKUP_BILL"));
        assertEquals(BillInquiryMode.DIRECT, BillInquiryMode.normalize("prepaid"));
        assertNull(BillInquiryMode.normalize(" "));
    }
}
