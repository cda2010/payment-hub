package com.gateway.payment.pricing.api;

import com.gateway.payment.pricing.api.dto.PricingDtos.QuoteResponse;
import com.gateway.payment.pricing.security.JwtAuthenticationFilter;
import com.gateway.payment.pricing.security.SecurityConfig;
import com.gateway.payment.pricing.service.PricingEngineService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PricingController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "pricing.security.jwt.secret=" + PricingControllerSecurityTest.SECRET,
        "pricing.security.jwt.issuer=payment-gateway",
        "pricing.security.cors.allowed-origins=http://localhost:4210"
})
class PricingControllerSecurityTest {

    static final String SECRET = "test-secret-test-secret-test-secret-test-secret-0123456789";
    private static final String QUOTE = "{\"productCode\":\"ACCOUNT_TRANSFER\",\"amount\":10000,\"currency\":\"XOF\"}";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PricingEngineService pricing;

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mvc.perform(get("/api/v1/pricing/policies"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void acceptsAccessTokenIssuedByPayment2() throws Exception {
        when(pricing.listPolicies()).thenReturn(List.of());

        mvc.perform(get("/api/v1/pricing/policies").header(HttpHeaders.AUTHORIZATION, bearer(SECRET, "access", "CUSTOMER")))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() throws Exception {
        String otherSecret = "another-secret-another-secret-another-secret-0123456789";

        mvc.perform(get("/api/v1/pricing/policies").header(HttpHeaders.AUTHORIZATION, bearer(otherSecret, "access", "ADMIN")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsRefreshToken() throws Exception {
        mvc.perform(get("/api/v1/pricing/policies").header(HttpHeaders.AUTHORIZATION, bearer(SECRET, "refresh", "ADMIN")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void quoteReturnsComputedFees() throws Exception {
        when(pricing.quote(any())).thenReturn(new QuoteResponse(
                true, List.of(), new BigDecimal("10000"), new BigDecimal("100"), null, new BigDecimal("10100"),
                new BigDecimal("10000"), null, "FEE-DOMESTIC", 1, "FIXED", null,
                "POL-TRANSFER-RETAIL", "SENDER", "FEE-DOMESTIC v1, tranche 1 : 100 = 100", UUID.randomUUID().toString(),
                new BigDecimal("10000"), "TRANSACTION_AMOUNT", null, null));

        mvc.perform(post("/api/v1/pricing/quote")
                        .header(HttpHeaders.AUTHORIZATION, bearer(SECRET, "access", "CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(QUOTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feeAmount").value(100))
                .andExpect(jsonPath("$.feeGridCode").value("FEE-DOMESTIC"))
                .andExpect(jsonPath("$.feeGridVersion").value(1))
                .andExpect(jsonPath("$.policyCode").value("POL-TRANSFER-RETAIL"));
    }

    @Test
    void invalidQuoteReturnsApiError400() throws Exception {
        when(pricing.quote(any())).thenThrow(new IllegalArgumentException("amount > 0 required"));

        mvc.perform(post("/api/v1/pricing/quote")
                        .header(HttpHeaders.AUTHORIZATION, bearer(SECRET, "access", "CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(QUOTE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("amount > 0 required"));
    }

    private static String bearer(String secret, String type, String... roles) {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .issuer("payment-gateway")
                .subject("jdupont")
                .claim("uid", UUID.randomUUID().toString())
                .claim("roles", List.of(roles))
                .claim("type", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(300)))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
        return "Bearer " + token;
    }
}
