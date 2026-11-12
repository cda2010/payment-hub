package com.gateway.payment.omnicanal.external;

import com.gateway.payment.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class ExternalOmnicanalClientTest {

    @Test
    void boundary_fetches_a_token_then_calls_the_external_service() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://omnicanal.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ExternalOmnicanalClient client = new ExternalOmnicanalClient(builder.build(), properties(true));

        server.expect(requestTo(containsString("/oauth/token")))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"access_token\":\"tok\",\"expires_in\":1800}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(containsString("/api/boundaryByTypeMethode")))
                .andExpect(method(POST))
                .andExpect(header("Authorization", "Bearer tok"))
                .andRespond(withSuccess("{\"errCode\":0,\"msg\":\"OK\"}", MediaType.APPLICATION_JSON));

        Map<String, Object> body = client.boundary("0011", "GETINFOS", ExternalOmnicanalClient.otrParametre(Map.of("telephone", "1")));

        assertEquals(0, ((Number) body.get("errCode")).intValue());
        assertEquals("OK", body.get("msg"));
        server.verify();
    }

    @Test
    void disabled_client_does_not_call_the_network() {
        ExternalOmnicanalClient client = new ExternalOmnicanalClient(RestClient.builder(), properties(false));

        BusinessException ex = assertThrows(BusinessException.class, () -> client.boundary("0011", "GETINFOS", ""));

        assertEquals("EXTERNAL_OMNICANAL_DISABLED", ex.getCode());
    }

    private static ExternalOmnicanalProperties properties(boolean enabled) {
        ExternalOmnicanalProperties properties = new ExternalOmnicanalProperties();
        properties.setEnabled(enabled);
        properties.setBaseUrl("http://omnicanal.test");
        properties.setLogin("admin");
        properties.setPassword("secret");
        properties.setClientId("Omnicanalapp");
        properties.setTimeout("PT5S");
        return properties;
    }
}
