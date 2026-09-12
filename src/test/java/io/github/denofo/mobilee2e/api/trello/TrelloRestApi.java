package io.github.denofo.mobilee2e.api.trello;

import io.github.denofo.mobilee2e.config.TrelloConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RedirectConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.Method;
import io.restassured.response.Response;

import java.net.URI;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Raw responses let integration tests assert both successful and rejected requests. */
public final class TrelloRestApi {
    private final URI baseUri;
    private final String authorization;

    public TrelloRestApi() {
        this(TrelloConfig.apiBaseUri(), TrelloConfig.apiKey(), TrelloConfig.apiToken());
    }

    TrelloRestApi(URI baseUri, String key, String token) {
        if (key == null || key.isBlank() || token == null || token.isBlank()) {
            throw new IllegalArgumentException("Trello credentials must not be blank.");
        }
        this.baseUri = URI.create(baseUri.toString().replaceAll("/+$", "") + "/");
        this.authorization = "OAuth oauth_consumer_key=\"" + escape(key)
                + "\", oauth_token=\"" + escape(token) + "\"";
    }

    public Response send(Method method, String path, Map<String, ?> parameters) {
        return send(method, path, parameters, true);
    }

    public Response withoutAuthorization(Method method, String path) {
        return send(method, path, Map.of(), false);
    }

    private Response send(Method method, String path, Map<String, ?> parameters, boolean authenticated) {
        var config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 10000)
                        .setParam("http.socket.timeout", 10000))
                .redirect(RedirectConfig.redirectConfig().followRedirects(false))
                .logConfig(LogConfig.logConfig().blacklistHeader("Authorization"));
        var builder = new RequestSpecBuilder()
                .setBaseUri(baseUri.toString())
                .setConfig(config)
                .addHeader("Accept", "application/json")
                .addQueryParams(parameters);
        if (authenticated) {
            builder.addHeader("Authorization", authorization);
        }
        try {
            // No request/response logging: credentials only travel in the header.
            return given().spec(builder.build()).request(method, path);
        } catch (Exception exception) {
            // Transport exceptions can include request details; do not expose their cause.
            throw new IllegalStateException("Trello transport failed: " + method + " " + path
                    + " (" + exception.getClass().getSimpleName() + ")");
        }
    }

    public static Response expectStatus(Response response, int expected, String operation) {
        // Do not dump response bodies on failure: a server may echo request credentials.
        io.qameta.allure.Allure.step(operation + ": expect HTTP " + expected,
                () -> assertEquals(expected, response.statusCode(), operation + ": HTTP status"));
        return response;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
