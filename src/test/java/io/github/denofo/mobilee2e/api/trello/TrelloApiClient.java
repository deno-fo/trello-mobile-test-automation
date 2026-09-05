package io.github.denofo.mobilee2e.api.trello;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.denofo.mobilee2e.config.TrelloConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public final class TrelloApiClient {

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI apiBaseUri;
    private final String apiKey;
    private final String apiToken;

    public TrelloApiClient() {
        this(
                TrelloConfig.apiBaseUri(),
                TrelloConfig.apiKey(),
                TrelloConfig.apiToken()
        );
    }

    TrelloApiClient(
            URI apiBaseUri,
            String apiKey,
            String apiToken
    ) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
        this.apiBaseUri = normalizeBaseUri(apiBaseUri);
        this.apiKey = requireNonBlank(apiKey, "Trello API key");
        this.apiToken = requireNonBlank(apiToken, "Trello API token");
    }

    public List<TrelloBoard> getOpenBoards() {
        URI uri = authenticatedUri(
                "members/me/boards",
                "filter=open&fields=id,name,closed"
        );

        String responseBody = send(
                HttpRequest.newBuilder(uri)
                        .timeout(REQUEST_TIMEOUT)
                        .header("Accept", "application/json")
                        .GET()
                        .build(),
                "get open boards"
        );

        try {
            return objectMapper.readValue(
                    responseBody,
                    new TypeReference<List<TrelloBoard>>() {
                    }
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not parse Trello boards response.",
                    exception
            );
        }
    }

    public Optional<TrelloBoard> findOpenBoardByName(
            String boardName
    ) {
        return getOpenBoards().stream()
                .filter(board -> boardName.equals(board.name()))
                .findFirst();
    }

    public void deleteBoard(String boardId) {
        URI uri = authenticatedUri(
                "boards/" + encodePathSegment(boardId),
                null
        );

        send(
                HttpRequest.newBuilder(uri)
                        .timeout(REQUEST_TIMEOUT)
                        .header("Accept", "application/json")
                        .DELETE()
                        .build(),
                "delete board"
        );
    }

    private String send(
            HttpRequest request,
            String operation
    ) {
        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Trello API request failed while trying to "
                                + operation
                                + ". HTTP status: "
                                + response.statusCode()
                );
            }

            return response.body();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Trello API request failed while trying to "
                            + operation
                            + ".",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Trello API request was interrupted while trying to "
                            + operation
                            + ".",
                    exception
            );
        }
    }

    private URI authenticatedUri(
            String relativePath,
            String query
    ) {
        StringBuilder builder = new StringBuilder(
                apiBaseUri.resolve(relativePath).toString()
        );

        builder.append('?');

        if (query != null && !query.isBlank()) {
            builder.append(query).append('&');
        }

        builder.append("key=")
                .append(encodeQueryValue(apiKey))
                .append("&token=")
                .append(encodeQueryValue(apiToken));

        return URI.create(builder.toString());
    }

    private static URI normalizeBaseUri(URI uri) {
        String raw = uri.toString();

        return URI.create(
                raw.endsWith("/")
                        ? raw
                        : raw + "/"
        );
    }

    private static String encodeQueryValue(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }

    private static String encodePathSegment(String value) {
        return encodeQueryValue(
                requireNonBlank(value, "Trello board id")
        ).replace("+", "%20");
    }

    private static String requireNonBlank(
            String value,
            String description
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    description + " must not be blank."
            );
        }

        return value.trim();
    }
}
