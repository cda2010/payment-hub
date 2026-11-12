package com.gateway.payment.network.api;

import com.gateway.payment.network.NetworkException;
import com.gateway.payment.network.api.dto.NetworkDtos.PageDto;
import com.gateway.payment.network.api.dto.NetworkDtos.RapportImport;
import com.gateway.payment.network.api.dto.NetworkDtos.ZoneDto;
import com.gateway.payment.network.domain.StatutUnite;
import com.gateway.payment.network.security.JwtAuthenticationFilter;
import com.gateway.payment.network.security.SecurityConfig;
import com.gateway.payment.network.service.ImportService;
import com.gateway.payment.network.service.PositionService;
import com.gateway.payment.network.service.RegroupementService;
import com.gateway.payment.network.service.UniteService;
import com.gateway.payment.network.service.ZoneService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UniteController.class, RegroupementController.class, ZoneController.class, ImportController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "network.security.jwt.secret=" + NetworkControllerSecurityTest.SECRET,
        "network.security.jwt.issuer=payment-gateway",
        "network.security.cors.allowed-origins=http://localhost:4200"
})
class NetworkControllerSecurityTest {

    static final String SECRET = "test-secret-test-secret-test-secret-test-secret-0123456789";
    private static final String AGENCE = "{\"code\":\"AG-X\",\"libelle\":\"Agence X\",\"type\":\"AGENCE\",\"parentId\":\""
            + UUID.randomUUID() + "\"}";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UniteService unites;

    @MockBean
    private RegroupementService regroupements;

    @MockBean
    private ZoneService zones;

    @MockBean
    private PositionService positions;

    @MockBean
    private ImportService imports;

    @Test
    void sansTokenRenvoie401() throws Exception {
        mvc.perform(get("/api/v1/network/unites"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void lectureOuverteATousLesUtilisateursConnectes() throws Exception {
        when(unites.rechercher(any(), any())).thenReturn(new PageDto<>(List.of(), 0, 0, 50));
        when(zones.lister()).thenReturn(List.of(new ZoneDto("UEMOA", "Union", "XOF", StatutUnite.ACTIF, 8, 20,
                null, null, null, null)));

        mvc.perform(get("/api/v1/network/unites").param("taille", "5000").param("page", "-3").param("tri", "inconnu")
                        .header(HttpHeaders.AUTHORIZATION, bearer("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
        verify(unites).rechercher(any(), eq(PageRequest.of(0, 500, Sort.by("niveau", "code"))));
        mvc.perform(get("/api/v1/network/referentiel").header(HttpHeaders.AUTHORIZATION, bearer("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zones[0].devise").value("XOF"))
                .andExpect(jsonPath("$.types[?(@.code=='SOUS_DISTRIBUTEUR')].parentsAutorises[0]").value("MASTER"));
        mvc.perform(get("/api/v1/network/zones").header(HttpHeaders.AUTHORIZATION, bearer("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nbPays").value(8));
    }

    @Test
    void lesZonesSeGerentParLesAdministrateurs() throws Exception {
        String zone = "{\"code\":\"EAC\",\"libelle\":\"Afrique de l'Est\",\"devise\":\"KES\"}";
        mvc.perform(post("/api/v1/network/zones")
                        .header(HttpHeaders.AUTHORIZATION, bearer("OPERATOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(zone))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/network/zones/{code}/desactiver", "UEMOA")
                        .header(HttpHeaders.AUTHORIZATION, bearer("CUSTOMER")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(zones);

        mvc.perform(post("/api/v1/network/zones")
                        .header(HttpHeaders.AUTHORIZATION, bearer("CONFIG_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(zone))
                .andExpect(status().isCreated());
        mvc.perform(put("/api/v1/network/zones/{code}", "EAC")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libelle\":\"EAC\",\"devise\":\"KESH\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    @Test
    void ecritureReserveeAuxAdministrateurs() throws Exception {
        mvc.perform(post("/api/v1/network/unites")
                        .header(HttpHeaders.AUTHORIZATION, bearer("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AGENCE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        mvc.perform(delete("/api/v1/network/regroupements/{id}/membres/{u}", UUID.randomUUID(), UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, bearer("OPERATOR")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(unites, regroupements);
    }

    @Test
    void importReserveAuxAdministrateurs() throws Exception {
        MockMultipartFile fichier = new MockMultipartFile("fichier", "unites.csv", "text/csv",
                "code;libelle;type;code_parent\nAG-X;Agence X;AGENCE;FSE-UEMOA\n".getBytes(StandardCharsets.UTF_8));
        mvc.perform(multipart("/api/v1/network/imports/unites").file(fichier)
                        .header(HttpHeaders.AUTHORIZATION, bearer("OPERATOR")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(imports);

        when(imports.importerUnites(eq("unites.csv"), any(), eq(true), eq("jdupont"))).thenReturn(new RapportImport(
                "UNITES", "CSV", true, false, 1, 1, 0, 0, 0, 0, List.of(), 12));
        mvc.perform(multipart("/api/v1/network/imports/unites").file(fichier)
                        .header(HttpHeaders.AUTHORIZATION, bearer("CONFIG_MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.essai").value(true))
                .andExpect(jsonPath("$.nbCreations").value(1));
        mvc.perform(multipart("/api/v1/network/imports/membres")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    @Test
    void creationParUnConfigManager() throws Exception {
        mvc.perform(post("/api/v1/network/unites")
                        .header(HttpHeaders.AUTHORIZATION, bearer("CONFIG_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AGENCE))
                .andExpect(status().isCreated());
    }

    @Test
    void regleDeGestionAuFormatApiError() throws Exception {
        when(unites.creer(any(), eq("jdupont")))
                .thenThrow(NetworkException.regle("RATTACHEMENT_INTERDIT", "Un(e) Agence se rattache à : FSE."));

        mvc.perform(post("/api/v1/network/unites")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AGENCE))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("RATTACHEMENT_INTERDIT"))
                .andExpect(jsonPath("$.path").value("/api/v1/network/unites"));
    }

    @Test
    void requeteInvalide() throws Exception {
        mvc.perform(post("/api/v1/network/unites")
                        .header(HttpHeaders.AUTHORIZATION, bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"AG X!\",\"libelle\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"))
                .andExpect(jsonPath("$.details.length()").value(3));
    }

    private static String bearer(String... roles) {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .issuer("payment-gateway")
                .subject("jdupont")
                .claim("roles", List.of(roles))
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(300)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
        return "Bearer " + token;
    }
}
