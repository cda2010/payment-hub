package com.gateway.payment.identity.api;

import com.gateway.payment.identity.api.dto.IdentityDtos.PartyDto;
import com.gateway.payment.identity.security.JwtAuthenticationFilter;
import com.gateway.payment.identity.security.SecurityConfig;
import com.gateway.payment.identity.service.IdentityService;
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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IdentityController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "identity.security.jwt.secret=" + IdentityControllerSecurityTest.SECRET,
        "identity.security.jwt.issuer=payment-gateway",
        "identity.security.cors.allowed-origins=http://localhost:4210"
})
class IdentityControllerSecurityTest {

    static final String SECRET = "test-secret-test-secret-test-secret-test-secret-0123456789";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private IdentityService identityService;

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mvc.perform(get("/api/v1/identity/parties"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void acceptsAccessTokenIssuedByPayment2() throws Exception {
        when(identityService.listParties(any())).thenReturn(List.of());

        mvc.perform(get("/api/v1/identity/parties").header(HttpHeaders.AUTHORIZATION, bearer(SECRET, "access", "OPERATOR")))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() throws Exception {
        String otherSecret = "another-secret-another-secret-another-secret-0123456789";

        mvc.perform(get("/api/v1/identity/parties").header(HttpHeaders.AUTHORIZATION, bearer(otherSecret, "access", "ADMIN")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsRefreshToken() throws Exception {
        mvc.perform(get("/api/v1/identity/parties").header(HttpHeaders.AUTHORIZATION, bearer(SECRET, "refresh", "ADMIN")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void creationRequiresWriteRole() throws Exception {
        mvc.perform(post("/api/v1/identity/parties")
                        .header(HttpHeaders.AUTHORIZATION, bearer(SECRET, "access", "AUDITOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"P1\",\"displayName\":\"Party 1\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void creationAllowedForAdmin() throws Exception {
        when(identityService.createParty(any())).thenReturn(new PartyDto(
                UUID.randomUUID(), "CORIS_DEMO", "P1", null, "Party 1", null, null, null,
                "STANDARD", "ACTIVE", true));

        mvc.perform(post("/api/v1/identity/parties")
                        .header(HttpHeaders.AUTHORIZATION, bearer(SECRET, "access", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"P1\",\"displayName\":\"Party 1\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void creationAllowedForCorporateGroupAdmin() throws Exception {
        when(identityService.createParty(any())).thenReturn(new PartyDto(
                UUID.randomUUID(), "CORIS_DEMO", "P1", null, "Party 1", null, null, null,
                "STANDARD", "ACTIVE", true));

        mvc.perform(post("/api/v1/identity/parties")
                        .header(HttpHeaders.AUTHORIZATION, bearer(SECRET, "access", "GROUP_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"P1\",\"displayName\":\"Party 1\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void unknownPartyReturnsApiError404() throws Exception {
        UUID id = UUID.randomUUID();
        when(identityService.getParty(id)).thenThrow(new NoSuchElementException("Party not found: " + id));

        mvc.perform(get("/api/v1/identity/parties/" + id).header(HttpHeaders.AUTHORIZATION, bearer(SECRET, "access", "OPERATOR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Party not found: " + id));
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
