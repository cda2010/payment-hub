package com.m2i.omnicanal.presentation.http;

import com.m2i.omnicanal.outils.restclient.constants.ErreurConstants;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PartnerHttpClientTest {

    @Test
    void mapsHttpStatusAndConnectionFailures() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/ok", exchange -> respond(exchange, 200, "{\"ok\":true}"));
        server.createContext("/missing", exchange -> respond(exchange, 404, ""));
        server.createContext("/down", exchange -> respond(exchange, 500, ""));
        server.start();
        try {
            PartnerHttpClient client = new PartnerHttpClient(RestClient.create());
            int port = server.getAddress().getPort();
            ResultMessage ok = client.call("http://127.0.0.1:" + port + "/ok", null, Map.of(), "GET", "1", "JSON", null);
            assertEquals(ErreurConstants.SUCCESS, ok.getErrCode());

            ResultMessage missing = client.call("http://127.0.0.1:" + port + "/missing", null, Map.of(), "GET", "1", "JSON", null);
            assertEquals(ErreurConstants.HTTP_CLIENT_STATUS_ERROR, missing.getErrCode());

            ResultMessage down = client.call("http://127.0.0.1:" + port + "/down", null, Map.of(), "GET", "1", "JSON", null);
            assertEquals(ErreurConstants.HTTP_CODE_STATUS_ERROR, down.getErrCode());
        } finally {
            server.stop(0);
        }

        int closed;
        try (ServerSocket socket = new ServerSocket(0)) {
            closed = socket.getLocalPort();
        }
        PartnerHttpClient client = new PartnerHttpClient(RestClient.create());
        ResultMessage refused = client.call("http://127.0.0.1:" + closed + "/x", null, Map.of(), "GET", "1", "JSON", null);
        assertEquals(ErreurConstants.RESSOURCE_ACCESS_ERROR, refused.getErrCode());
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, int status, String body) throws java.io.IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
