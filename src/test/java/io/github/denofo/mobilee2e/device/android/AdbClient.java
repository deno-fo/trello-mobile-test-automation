package io.github.denofo.mobilee2e.device.android;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class AdbClient {

    private final String adbPath;

    public AdbClient() {
        this(resolveAdbPath());
    }

    AdbClient(String adbPath) {
        if (adbPath == null || adbPath.isBlank()) {
            throw new IllegalArgumentException("ADB path must not be blank.");
        }

        this.adbPath = adbPath;
    }

    public String run(String... arguments) {
        List<String> command = new ArrayList<>();
        command.add(adbPath);
        command.addAll(List.of(arguments));

        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();

            String output = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IllegalStateException(
                        "ADB command failed: "
                                + String.join(" ", command)
                                + ". Output: "
                                + output
                );
            }

            return output;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not start adb. Resolved command: "
                            + adbPath
                            + ". Configure ANDROID_HOME or ANDROID_SDK_ROOT, "
                            + "or add adb to PATH.",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "ADB command was interrupted.",
                    exception
            );
        }
    }

    public boolean isPackageInstalled(
            String udid,
            String packageName
    ) {
        String output = run(
                "-s",
                udid,
                "shell",
                "pm",
                "path",
                packageName
        ).trim();

        return output.startsWith("package:");
    }

    public void clearAppData(
            String udid,
            String packageName
    ) {
        String output = run(
                "-s",
                udid,
                "shell",
                "pm",
                "clear",
                packageName
        ).trim();

        if (!output.contains("Success")) {
            throw new IllegalStateException(
                    "Failed to clear app data for "
                            + packageName
                            + " on device "
                            + udid
                            + ". ADB output: "
                            + output
            );
        }
    }

    private static String resolveAdbPath() {
        String executableName = isWindows()
                ? "adb.exe"
                : "adb";

        for (String environmentVariable : List.of(
                "ANDROID_HOME",
                "ANDROID_SDK_ROOT"
        )) {
            String sdkRoot = System.getenv(environmentVariable);

            if (sdkRoot == null || sdkRoot.isBlank()) {
                continue;
            }

            Path adbPath = Path.of(
                    sdkRoot,
                    "platform-tools",
                    executableName
            );

            if (Files.isRegularFile(adbPath)) {
                return adbPath.toString();
            }
        }

        return executableName;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name")
                .startsWith("Windows");
    }
}
