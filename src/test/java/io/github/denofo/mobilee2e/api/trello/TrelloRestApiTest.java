package io.github.denofo.mobilee2e.api.trello;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static io.restassured.http.Method.*;
import static org.junit.jupiter.api.Assertions.*;

class TrelloRestApiTest {
    private HttpServer server;
    private TrelloRestApi api;
    private final AtomicReference<String> auth = new AtomicReference<>();
    private final AtomicReference<String> method = new AtomicReference<>();
    private final AtomicReference<String> query = new AtomicReference<>();

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/1/cards", exchange -> {
            auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            method.set(exchange.getRequestMethod());
            query.set(exchange.getRequestURI().getRawQuery());
            byte[] body = "{\"id\":\"card-1\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.createContext("/1/redirect", exchange -> {
            exchange.getResponseHeaders().add("Location", "/1/cards");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.start();
        api = new TrelloRestApi(URI.create("http://127.0.0.1:" + server.getAddress().getPort()
                + "/1/"), "fake-key", "fake-token");
    }

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }

    @Test
    void sendsHeaderNotCredentialQueryAndPreservesParameters() {
        var response = api.send(PUT, "cards", Map.of("name", "Name + & карточка"));
        assertEquals(200, response.statusCode());
        assertEquals("PUT", method.get());
        assertEquals("OAuth oauth_consumer_key=\"fake-key\", oauth_token=\"fake-token\"", auth.get());
        assertFalse(query.get().contains("fake-key"));
        assertFalse(query.get().contains("fake-token"));
        assertEquals("name=Name + & карточка",
                java.net.URLDecoder.decode(query.get(), StandardCharsets.UTF_8));
        assertEquals("card-1", response.jsonPath().getString("id"));
    }

    @Test
    void omitsAuthorizationForNegativeTest() {
        api.withoutAuthorization(GET, "cards");
        assertNull(auth.get());
        assertNull(query.get());
    }

    @Test
    void doesNotFollowRedirectsWithCredentials() {
        assertEquals(302, api.send(GET, "redirect", Map.of()).statusCode());
        assertNull(auth.get(), "Redirect destination must not be called");
    }

    @Test
    void statusFailureDoesNotDumpResponseBody() {
        var response = api.send(GET, "cards", Map.of());
        var failure = assertThrows(AssertionError.class,
                () -> TrelloRestApi.expectStatus(response, 401, "negative test"));
        assertFalse(failure.getMessage().contains("card-1"));
        assertFalse(failure.getMessage().contains("fake-token"));
        assertTrue(failure.getMessage().contains("HTTP status"));
    }
}
