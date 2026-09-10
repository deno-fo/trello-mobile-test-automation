package io.github.denofo.mobilee2e.diagnostics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FailureArtifactsTest {

    @TempDir
    Path root;

    @Test
    void savesBothArtifactsWithoutChangingContent() throws Exception {
        byte[] image = {1, 2, 3};
        String xml = "<screen text=\"Описание\"/>";
        Path directory = FailureArtifacts.capture(root, "test-device",
                () -> image, () -> xml);
        assertArrayEquals(image, Files.readAllBytes(directory.resolve("screenshot.png")));
        assertEquals(xml, Files.readString(directory.resolve("page-source.xml")));
    }

    @Test
    void stillSavesXmlWhenScreenshotFails() throws Exception {
        Path directory = FailureArtifacts.capture(root, "test",
                () -> { throw new IllegalStateException("session lost"); }, () -> "<screen/>");
        assertFalse(Files.exists(directory.resolve("screenshot.png")));
        assertEquals("<screen/>", Files.readString(directory.resolve("page-source.xml")));
    }

    @Test
    void retainsScreenshotWhenXmlFails() throws Exception {
        Path directory = FailureArtifacts.capture(root, "test", () -> new byte[]{1},
                () -> { throw new IllegalStateException("session lost"); });
        assertTrue(Files.exists(directory.resolve("screenshot.png")));
        assertFalse(Files.exists(directory.resolve("page-source.xml")));
    }

    @Test
    void sanitizesNamesAndDoesNotOverwritePreviousRuns() throws Exception {
        String label = "../../device:name/" + "x".repeat(300);
        Path first = FailureArtifacts.capture(root, label, () -> new byte[]{1}, () -> "<a/>");
        Path second = FailureArtifacts.capture(root, label, () -> new byte[]{2}, () -> "<b/>");
        assertEquals(root, first.getParent());
        assertEquals(root, second.getParent());
        assertNotEquals(first, second);
        assertArrayEquals(new byte[]{1}, Files.readAllBytes(first.resolve("screenshot.png")));
    }
}
