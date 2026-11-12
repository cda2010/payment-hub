package com.gateway.payment.catalog.journey;

import com.gateway.payment.common.domain.AccessChannel;
import com.gateway.payment.common.domain.ProductAudience;
import com.gateway.payment.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CatalogJourneySettingsTest {

    @Test
    void resolvesDefaultChannelAndJourney() {
        CatalogJourneySettings settings = CatalogJourneySettings.from(Map.of(
                "journeyCode", "STANDARD_CASH_OUT",
                "supportedChannels", List.of("MOBILE", "USSD"),
                "defaultChannel", "MOBILE"
        ));
        assertEquals("STANDARD_CASH_OUT", settings.resolveJourneyCode(null));
        assertEquals(AccessChannel.MOBILE, settings.resolveChannel(null));
        assertEquals(AccessChannel.USSD, settings.resolveChannel("ussd"));
    }

    @Test
    void rejectsUnsupportedChannel() {
        CatalogJourneySettings settings = CatalogJourneySettings.from(Map.of(
                "supportedChannels", List.of("MOBILE"),
                "defaultChannel", "MOBILE"
        ));
        BusinessException ex = assertThrows(BusinessException.class, () -> settings.resolveChannel("GAB"));
        assertEquals("ACCESS_CHANNEL_UNSUPPORTED", ex.getCode());
    }

    @Test
    void mergesChannelBindingOverlay() {
        CatalogJourneySettings settings = CatalogJourneySettings.from(Map.of(
                "journeyCode", "STANDARD_CASH_OUT",
                "bindings", List.of(Map.of(
                        "step", "CAPTURE_AMOUNT",
                        "kind", "UI",
                        "formKey", "cash-out-amount"
                )),
                "channelDefaults", Map.of(
                        "USSD", Map.of(
                                "bindings", List.of(Map.of(
                                        "step", "CAPTURE_AMOUNT",
                                        "formKey", "cash-out-amount-ussd"
                                ))
                        )
                )
        ));
        Map<String, Object> effective = settings.effectiveConfiguration(AccessChannel.USSD);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> bindings = (List<Map<String, Object>>) effective.get("bindings");
        assertEquals(1, bindings.size());
        assertEquals("cash-out-amount-ussd", bindings.get(0).get("formKey"));
        assertEquals("UI", bindings.get(0).get("kind"));
    }

    @Test
    void resolvesAudienceAndRejectsUnsupported() {
        CatalogJourneySettings settings = CatalogJourneySettings.from(Map.of(
                "supportedAudiences", List.of("RETAIL", "CORPORATE"),
                "defaultAudience", "RETAIL"
        ));
        assertEquals(ProductAudience.RETAIL, settings.resolveAudience(null));
        assertEquals(ProductAudience.CORPORATE, settings.resolveAudience("corporate"));
        assertTrue(settings.supportsAudience(ProductAudience.RETAIL));
        assertFalse(settings.supportsAudience(ProductAudience.AGENT));
        BusinessException ex = assertThrows(
                BusinessException.class, () -> settings.resolveAudience("AGENT"));
        assertEquals("PRODUCT_AUDIENCE_UNSUPPORTED", ex.getCode());
    }

    @Test
    void audienceAliasMarchand() {
        CatalogJourneySettings settings = CatalogJourneySettings.from(Map.of(
                "supportedAudiences", List.of("MERCHANT"),
                "defaultAudience", "MARCHAND"
        ));
        assertEquals(ProductAudience.MERCHANT, settings.resolveAudience(null));
        assertTrue(settings.supportsAudience("marchand"));
    }

    @Test
    void noAudiencePolicyAcceptsAny() {
        CatalogJourneySettings settings = CatalogJourneySettings.from(Map.of(
                "supportedChannels", List.of("MOBILE")
        ));
        assertFalse(settings.hasAudiencePolicy());
        assertNull(settings.resolveAudience(null));
        assertTrue(settings.supportsAudience(ProductAudience.CORPORATE));
    }
}
