package io.github.denofo.mobilee2e.api.trello;

public record TrelloBoard(
        String id,
        String name,
        boolean closed
) {
}
