package io.github.denofo.mobilee2e.diagnostics;

import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public final class FailureArtifacts {

    private FailureArtifacts() {
    }

    public static Path capture(Path root, String label,
                               Supplier<byte[]> screenshot,
                               Supplier<String> pageSource) throws IOException {
        Files.createDirectories(root);
        String safeLabel = label.replaceAll("[^a-zA-Z0-9._-]", "_");
        safeLabel = safeLabel.substring(0, Math.min(safeLabel.length(), 100));
        Path directory = Files.createTempDirectory(root, safeLabel + "-");

        // Each artifact is independent: a broken screenshot must not prevent XML capture.
        save(directory.resolve("screenshot.png"), screenshot);
        save(directory.resolve("page-source.xml"),
                () -> pageSource.get().getBytes(StandardCharsets.UTF_8));
        System.err.println("Failure artifacts directory: " + directory.toAbsolutePath());
        return directory;
    }

    private static void save(Path file, Supplier<byte[]> content) {
        try {
            byte[] bytes = content.get();
            Files.write(file, bytes);
            if (Allure.getLifecycle().getCurrentTestCase().isPresent()) {
                boolean png = file.getFileName().toString().endsWith(".png");
                Allure.addAttachment(file.getFileName().toString(),
                        png ? "image/png" : "application/xml",
                        new ByteArrayInputStream(bytes), png ? ".png" : ".xml");
            }
        } catch (Exception | AssertionError failure) {
            // Avoid logging driver responses, which may contain private screen data.
            System.err.println("Could not save " + file.getFileName()
                    + " (" + failure.getClass().getSimpleName() + ")");
        }
    }
}
