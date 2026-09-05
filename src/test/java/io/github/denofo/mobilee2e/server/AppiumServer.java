package io.github.denofo.mobilee2e.server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class AppiumServer {

    private static final Duration CHECK_TIMEOUT =
            Duration.ofSeconds(3);

    private static final HttpClient HTTP_CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(CHECK_TIMEOUT)
                    .build();

    private AppiumServer() {
    }

    public static void ensureAvailable(URI serverUri) {
        URI statusUri = serverUri.resolve("/status");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(statusUri)
                .timeout(CHECK_TIMEOUT)
                .GET()
                .build();

        try {
            HttpResponse<Void> response =
                    HTTP_CLIENT.send(
                            request,
                            HttpResponse.BodyHandlers.discarding()
                    );

            int statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode < 300) {
                return;
            }

            throw new IllegalStateException(
                    "Appium server returned HTTP "
                            + statusCode
                            + " for "
                            + statusUri
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Appium server is not available at "
                            + statusUri
                            + ". Start Appium and retry.",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Appium server availability check was interrupted.",
                    exception
            );
        }
    }
}
