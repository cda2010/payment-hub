package com.gateway.payment.edge;

import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureObservability
class ApiGatewayRouteTest {

    private static final String SECRET = "test-secret-at-least-32-bytes-long";

    private static HttpServer identity;
    private static final AtomicReference<String> path = new AtomicReference<>();
    private static final AtomicReference<String> authorization = new AtomicReference<>();

    @DynamicPropertySource
    static void routes(DynamicPropertyRegistry registry) throws IOException {
        identity = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        identity.createContext("/api/v1/identity/", exchange -> {
            path.set(exchange.getRequestURI().getPath());
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = "{\"indicatif\":\"226\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://127.0.0.1:4205");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        identity.start();
        String origin = "http://127.0.0.1:" + identity.getAddress().getPort();
        registry.add("IDENTITY_URL", () -> origin);
        registry.add("ALIAS_URL", () -> "http://127.0.0.1:1");
        registry.add("CONTENT_URL", () -> "http://127.0.0.1:1");
        registry.add("DIVERS_URL", () -> "http://127.0.0.1:1");
        registry.add("FIDELITE_URL", () -> "http://127.0.0.1:1");
        registry.add("ENGINE_URL", () -> "http://127.0.0.1:1");
        registry.add("management.server.port", () -> "0");
        registry.add("edge.security.keycloak.enabled", () -> "false");
        registry.add("edge.security.jwt.secret", () -> SECRET);
        registry.add("edge.security.jwt.issuer", () -> "payment-gateway");
    }

    @AfterAll
    static void stop() {
        if (identity != null) {
            identity.stop(0);
        }
    }

    @Autowired
    private TestRestTemplate rest;

    @LocalManagementPort
    private int managementPort;

    @Test
    void identityPasseParLaPorte() {
        String jeton = jeton();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ORIGIN, "http://127.0.0.1:4205");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jeton);

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/identity/enrolement/indicatifs",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("226"));
        assertEquals("/api/v1/identity/enrolement/indicatifs", path.get());
        assertEquals("Bearer " + jeton, authorization.get());
        List<String> origins = response.getHeaders().get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        assertEquals(List.of("http://127.0.0.1:4205"), origins);
    }

    @Test
    void laPorteRepondElleMeme() {
        ResponseEntity<String> response = management("/actuator/health");
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("UP"));
    }

    @Test
    void lesMesuresRestentHorsDeLaPortePublique() {
        ResponseEntity<String> response = rest.getForEntity("/actuator/prometheus", String.class);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void prometheusCompteLesRoutes() {
        rest.getForEntity("/api/v1/identity/enrolement/indicatifs", String.class);

        ResponseEntity<String> response = management("/actuator/prometheus");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("spring_cloud_gateway_requests"));
        assertTrue(response.getBody().contains("routeId=\"identity\""));
        assertTrue(response.getBody().contains("payment_edge_requests_total"));
        assertTrue(response.getBody().contains("route=\"identity\""));
        assertTrue(response.getBody().contains("outcome=\"success\""));
        assertTrue(response.getBody().contains("payment_edge_latency_seconds"));
    }

    @Test
    void unJetonAbsentOuFauxResteALaPorte() {
        path.set(null);
        ResponseEntity<String> absent = rest.getForEntity("/api/v1/identity/moi", String.class);
        assertEquals(401, absent.getStatusCode().value());
        assertEquals(null, path.get());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer jeton-de-test");
        ResponseEntity<String> faux = rest.exchange(
                "/api/v1/identity/moi", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(401, faux.getStatusCode().value());
        assertEquals(null, path.get());
    }

    private static String jeton() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("poste")
                .issuer("payment-gateway")
                .claim("type", "access")
                .expiration(Date.from(Instant.now().plusSeconds(120)))
                .signWith(key)
                .compact();
    }

    private ResponseEntity<String> management(String path) {
        return new TestRestTemplate().getForEntity("http://127.0.0.1:" + managementPort + path, String.class);
    }
}
