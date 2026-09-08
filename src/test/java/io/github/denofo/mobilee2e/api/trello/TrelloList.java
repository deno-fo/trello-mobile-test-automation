package io.github.denofo.mobilee2e.api.trello;

public record TrelloList(
        String id,
        String name,
        boolean closed
) {
}
