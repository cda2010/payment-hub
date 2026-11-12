package com.gateway.payment.omnicanal.external;

import com.gateway.payment.configuration.api.dto.ProviderResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OmnicanalProviderMatchTest {

    @Test
    void railNameResolvesToTheBurkinaFse() {
        List<ProviderResponse> providers = List.of(
                fse("0015", "Coris Money Senegal", "Senegal"),
                fse("0011", "CORIS MONEY CBI BURKINA", "Burkina Faso"),
                fse("0001", "Orange Money", "Burkina Faso"));

        assertEquals("0011", OmnicanalProviderMatch.best("CORIS_MONEY", providers).orElseThrow().codeFse());
        assertEquals("0001", OmnicanalProviderMatch.best("ORANGE_MONEY", providers).orElseThrow().codeFse());
        assertEquals("0011", OmnicanalProviderMatch.best("0011", providers).orElseThrow().codeFse());
        assertTrue(OmnicanalProviderMatch.best("INCONNU", providers).isEmpty());
    }

    private static ProviderResponse fse(String code, String nom, String pays) {
        return new ProviderResponse(
                UUID.nameUUIDFromBytes(code.getBytes()),
                code,
                nom,
                null,
                null,
                true,
                null,
                null,
                null,
                List.of(),
                code,
                null,
                null,
                pays,
                null,
                null,
                null,
                null);
    }
}
