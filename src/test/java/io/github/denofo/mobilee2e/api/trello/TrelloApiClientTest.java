package io.github.denofo.mobilee2e.api.trello;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrelloApiClientTest {

    private HttpServer server;
    private URI apiBaseUri;
    private final AtomicReference<String> requestMethod =
            new AtomicReference<>();
    private final AtomicReference<String> requestPath =
            new AtomicReference<>();
    private final AtomicReference<String> requestQuery =
            new AtomicReference<>();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0),
                0
        );
        server.start();

        apiBaseUri = URI.create(
                "http://127.0.0.1:"
                        + server.getAddress().getPort()
                        + "/1/"
        );
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldGetOpenBoardsAndIgnoreUnknownFields() {
        server.createContext(
                "/1/members/me/boards",
                exchange -> respond(
                        exchange,
                        200,
                        """
                        [
                          {
                            "id": "board-1",
                            "name": "E2E Board",
                            "closed": false,
                            "unexpected": "ignored"
                          }
                        ]
                        """
                )
        );

        TrelloApiClient client = new TrelloApiClient(
                apiBaseUri,
                "test-key",
                "test-token"
        );

        List<TrelloBoard> boards = client.getOpenBoards();

        assertEquals(1, boards.size());
        assertEquals("board-1", boards.get(0).id());
        assertEquals("E2E Board", boards.get(0).name());
        assertFalse(boards.get(0).closed());
        assertEquals("GET", requestMethod.get());
        assertEquals("/1/members/me/boards", requestPath.get());
        assertTrue(requestQuery.get().contains("filter=open"));
        assertTrue(requestQuery.get().contains("fields=id,name,closed"));
        assertTrue(requestQuery.get().contains("key=test-key"));
        assertTrue(requestQuery.get().contains("token=test-token"));
    }

    @Test
    void shouldDeleteBoardById() {
        server.createContext(
                "/1/boards/board-123",
                exchange -> respond(exchange, 200, "{}")
        );

        TrelloApiClient client = new TrelloApiClient(
                apiBaseUri,
                "test-key",
                "test-token"
        );

        client.deleteBoard("board-123");

        assertEquals("DELETE", requestMethod.get());
        assertEquals("/1/boards/board-123", requestPath.get());
        assertTrue(requestQuery.get().contains("key=test-key"));
        assertTrue(requestQuery.get().contains("token=test-token"));
    }

    private void respond(
            HttpExchange exchange,
            int statusCode,
            String responseBody
    ) throws IOException {
        requestMethod.set(exchange.getRequestMethod());
        requestPath.set(exchange.getRequestURI().getPath());
        requestQuery.set(exchange.getRequestURI().getRawQuery());

        byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add(
                "Content-Type",
                "application/json"
        );
        exchange.sendResponseHeaders(statusCode, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
