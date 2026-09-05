package io.github.denofo.mobilee2e.config;

import java.util.Optional;

public final class ConfigResolver {

    private ConfigResolver() {
    }

    public static Optional<String> optional(
            String systemProperty,
            String environmentVariable
    ) {
        String propertyValue =
                System.getProperty(systemProperty);

        if (propertyValue != null
                && !propertyValue.isBlank()) {
            return Optional.of(propertyValue.trim());
        }

        String environmentValue =
                System.getenv(environmentVariable);

        if (environmentValue != null
                && !environmentValue.isBlank()) {
            return Optional.of(environmentValue.trim());
        }

        return Optional.empty();
    }

    public static String required(
            String systemProperty,
            String environmentVariable
    ) {
        return optional(
                systemProperty,
                environmentVariable
        ).orElseThrow(
                () -> new IllegalStateException(
                        "Missing configuration. Set -D"
                                + systemProperty
                                + "=<value> or environment variable "
                                + environmentVariable
                )
        );
    }
}
