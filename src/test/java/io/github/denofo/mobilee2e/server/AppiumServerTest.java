package io.github.denofo.mobilee2e.server;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppiumServerTest {

    @Test
    void statusUriShouldWorkForRootServerUrl() {
        URI actual = AppiumServer.statusUri(
                URI.create("http://127.0.0.1:4723")
        );

        assertEquals(
                URI.create("http://127.0.0.1:4723/status"),
                actual
        );
    }

    @Test
    void statusUriShouldPreserveBasePath() {
        URI actual = AppiumServer.statusUri(
                URI.create("http://127.0.0.1:4723/wd/hub")
        );

        assertEquals(
                URI.create("http://127.0.0.1:4723/wd/hub/status"),
                actual
        );
    }
}
