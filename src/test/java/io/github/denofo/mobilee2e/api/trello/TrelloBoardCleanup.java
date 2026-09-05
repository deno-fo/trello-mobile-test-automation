package io.github.denofo.mobilee2e.api.trello;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public final class TrelloBoardCleanup implements AutoCloseable {

    private static final Duration LOOKUP_TIMEOUT =
            Duration.ofSeconds(15);

    private static final Duration POLL_INTERVAL =
            Duration.ofMillis(500);

    private final TrelloApiClient apiClient;
    private final String boardName;

    public TrelloBoardCleanup(String boardName) {
        this(new TrelloApiClient(), boardName);
    }

    TrelloBoardCleanup(
            TrelloApiClient apiClient,
            String boardName
    ) {
        this.apiClient = apiClient;
        this.boardName = requireNonBlank(boardName);
    }

    @Override
    public void close() {
        Instant deadline = Instant.now().plus(LOOKUP_TIMEOUT);

        do {
            Optional<TrelloBoard> board =
                    apiClient.findOpenBoardByName(boardName);

            if (board.isPresent()) {
                apiClient.deleteBoard(board.get().id());
                return;
            }

            sleep(POLL_INTERVAL);
        } while (Instant.now().isBefore(deadline));
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while waiting for Trello board cleanup.",
                    exception
            );
        }
    }

    private static String requireNonBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Trello board name must not be blank."
            );
        }

        return value.trim();
    }
}
