package io.github.denofo.mobilee2e.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public final class ConfigResolver {

    private static final Path LOCAL_CONFIG_PATH =
            Path.of("local.properties");

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

        return localProperty(systemProperty);
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
                                + ", or add "
                                + systemProperty
                                + "=<value> to local.properties"
                )
        );
    }

    private static Optional<String> localProperty(
            String propertyName
    ) {
        if (!Files.isRegularFile(LOCAL_CONFIG_PATH)) {
            return Optional.empty();
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(
                LOCAL_CONFIG_PATH
        )) {
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read local configuration from "
                            + LOCAL_CONFIG_PATH.toAbsolutePath(),
                    exception
            );
        }

        String value = properties.getProperty(propertyName);

        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(value.trim());
    }
}
