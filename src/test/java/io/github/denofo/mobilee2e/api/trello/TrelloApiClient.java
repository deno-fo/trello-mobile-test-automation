package io.github.denofo.mobilee2e.api.trello;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
    private final String authorizationHeader;

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
        this.objectMapper = new ObjectMapper()
                .configure(
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        false
                );
        this.apiBaseUri = normalizeBaseUri(apiBaseUri);
        this.authorizationHeader = authorizationHeader(
                apiKey,
                apiToken
        );
    }

    public TrelloBoard createBoard(String name) {
        return post("boards/", "name=" + encodePathSegment(name)
                + "&defaultLists=false&prefs_permissionLevel=private", TrelloBoard.class);
    }

    public TrelloList createList(String boardId, String name) {
        return post("lists", "name=" + encodePathSegment(name)
                + "&idBoard=" + encodePathSegment(boardId) + "&pos=bottom", TrelloList.class);
    }

    public TrelloCard createCard(String listId, String name) {
        return post("cards", "name=" + encodePathSegment(name)
                + "&idList=" + encodePathSegment(listId), TrelloCard.class);
    }

    private <T> T post(String path, String query, Class<T> type) {
        String body = send(requestBuilder(apiUri(path, query))
                .POST(HttpRequest.BodyPublishers.noBody()).build(), "create " + type.getSimpleName());
        try {
            return objectMapper.readValue(body, type);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not parse created " + type.getSimpleName() + ".", exception);
        }
    }

    public List<TrelloBoard> getOpenBoards() {
        URI uri = apiUri(
                "members/me/boards",
                "filter=open&fields=id,name,closed"
        );

        String responseBody = send(
                requestBuilder(uri)
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

    public List<TrelloList> getOpenLists(String boardId) {
        URI uri = apiUri(
                "boards/" + encodePathSegment(boardId) + "/lists",
                "filter=open&fields=id,name,closed"
        );

        String responseBody = send(
                requestBuilder(uri)
                        .GET()
                        .build(),
                "get open lists"
        );

        try {
            return objectMapper.readValue(
                    responseBody,
                    new TypeReference<List<TrelloList>>() {
                    }
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not parse Trello lists response.",
                    exception
            );
        }
    }

    public Optional<TrelloList> findOpenListByName(
            String boardId,
            String listName
    ) {
        return getOpenLists(boardId).stream()
                .filter(list -> listName.equals(list.name()))
                .findFirst();
    }

    public TrelloList awaitOpenListByName(String boardId, String listName) {
        return awaitOpenListByName(boardId, listName, 5, Duration.ofMillis(500));
    }

    TrelloList awaitOpenListByName(
            String boardId, String listName, int attempts, Duration interval
    ) {
        if (attempts < 1 || interval.isNegative()) {
            throw new IllegalArgumentException("Positive attempts and non-negative interval required.");
        }
        List<TrelloList> lastLists = List.of();
        for (int attempt = 1; attempt <= attempts; attempt++) {
            // HTTP and parsing failures propagate immediately, rather than being retried.
            lastLists = getOpenLists(boardId);
            Optional<TrelloList> found = lastLists.stream()
                    .filter(list -> listName.equals(list.name()))
                    .findFirst();
            if (found.isPresent()) {
                return found.get();
            }
            if (attempt < attempts) {
                try {
                    Thread.sleep(interval.toMillis());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for Trello list.", exception);
                }
            }
        }
        throw new AssertionError("Trello list not found after " + attempts
                + " API requests. Board id=" + boardId + ", expected name=\"" + listName
                + "\". Last returned open lists=" + lastLists);
    }

    public List<TrelloCard> getCards(String listId) {
        URI uri = apiUri(
                "lists/" + encodePathSegment(listId) + "/cards",
                null
        );

        String responseBody = send(
                requestBuilder(uri)
                        .GET()
                        .build(),
                "get cards"
        );

        try {
            return objectMapper.readValue(
                    responseBody,
                    new TypeReference<List<TrelloCard>>() {
                    }
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not parse Trello cards response.",
                    exception
            );
        }
    }

    public Optional<TrelloCard> findCardByName(
            String listId,
            String cardName
    ) {
        return getCards(listId).stream()
                .filter(card -> cardName.equals(card.name()))
                .findFirst();
    }

    public TrelloCard awaitCardByName(String listId, String cardName) {
        return awaitCardByName(listId, cardName, 5, Duration.ofMillis(500));
    }

    TrelloCard awaitCardByName(
            String listId, String cardName, int attempts, Duration interval
    ) {
        if (attempts < 1 || interval.isNegative()) {
            throw new IllegalArgumentException("Positive attempts and non-negative interval required.");
        }
        List<TrelloCard> lastCards = List.of();
        for (int attempt = 1; attempt <= attempts; attempt++) {
            // Retry missing data only; HTTP and parsing failures propagate immediately.
            lastCards = getCards(listId);
            Optional<TrelloCard> found = lastCards.stream()
                    .filter(card -> cardName.equals(card.name()))
                    .findFirst();
            if (found.isPresent()) {
                return found.get();
            }
            if (attempt < attempts) {
                try {
                    Thread.sleep(interval.toMillis());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for Trello card.", exception);
                }
            }
        }
        // Do not dump descriptions or the raw API response into test logs.
        var actualCards = lastCards.stream()
                .map(card -> "{id=" + card.id() + ", name=" + card.name()
                        + ", idList=" + card.idList() + ", closed=" + card.closed() + "}")
                .toList();
        throw new AssertionError("Trello card not found after " + attempts
                + " API requests. List id=" + listId + ", expected name=\"" + cardName
                + "\". Last returned cards=" + actualCards);
    }

    public void deleteBoard(String boardId) {
        URI uri = apiUri(
                "boards/" + encodePathSegment(boardId),
                null
        );

        send(
                requestBuilder(uri)
                        .DELETE()
                        .build(),
                "delete board"
        );
    }

    private HttpRequest.Builder requestBuilder(URI uri) {
        return HttpRequest.newBuilder(uri)
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .header("Authorization", authorizationHeader);
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

    private URI apiUri(
            String relativePath,
            String query
    ) {
        String rawUri = apiBaseUri.resolve(relativePath).toString();

        if (query == null || query.isBlank()) {
            return URI.create(rawUri);
        }

        return URI.create(rawUri + "?" + query);
    }

    private static String authorizationHeader(
            String apiKey,
            String apiToken
    ) {
        return "OAuth oauth_consumer_key=\""
                + escapeHeaderValue(
                requireNonBlank(apiKey, "Trello API key")
        )
                + "\", oauth_token=\""
                + escapeHeaderValue(
                requireNonBlank(apiToken, "Trello API token")
        )
                + "\"";
    }

    private static URI normalizeBaseUri(URI uri) {
        String raw = uri.toString();

        return URI.create(
                raw.endsWith("/")
                        ? raw
                        : raw + "/"
        );
    }

    private static String encodePathSegment(String value) {
        return URLEncoder.encode(
                requireNonBlank(value, "Trello board id"),
                StandardCharsets.UTF_8
        ).replace("+", "%20");
    }

    private static String escapeHeaderValue(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
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
