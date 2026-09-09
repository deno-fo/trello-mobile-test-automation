package io.github.denofo.mobilee2e.api.trello;

public record TrelloCard(
        String id,
        String name,
        boolean closed,
        String idList
) {
}
