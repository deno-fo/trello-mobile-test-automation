package io.github.denofo.mobilee2e.config;

import java.net.URI;

public final class TrelloConfig {

    private static final URI DEFAULT_API_BASE_URI =
            URI.create("https://api.trello.com/1/");

    private TrelloConfig() {
    }

    public static URI apiBaseUri() {
        String configured = ConfigResolver.optional(
                "trello.apiBaseUrl",
                "TRELLO_API_BASE_URL"
        ).orElse(DEFAULT_API_BASE_URI.toString());

        return URI.create(
                configured.endsWith("/")
                        ? configured
                        : configured + "/"
        );
    }

    public static String apiKey() {
        return ConfigResolver.required(
                "trello.apiKey",
                "TRELLO_API_KEY"
        );
    }

    public static String apiToken() {
        return ConfigResolver.required(
                "trello.apiToken",
                "TRELLO_API_TOKEN"
        );
    }
}
