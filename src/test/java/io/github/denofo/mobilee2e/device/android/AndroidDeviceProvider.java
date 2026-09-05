package io.github.denofo.mobilee2e.device.android;

import io.github.denofo.mobilee2e.config.FrameworkConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class AndroidDeviceProvider {

    private static final String ADB_PATH = resolveAdbPath();

    private AndroidDeviceProvider() {
    }

    public static List<AndroidDevice> discover() {
        List<String> connectedDevices = getConnectedDeviceUdids();

        FrameworkConfig.androidUdid().ifPresent(configuredUdid -> {
            if (!connectedDevices.contains(configuredUdid)) {
                throw new IllegalStateException(
                        "Configured Android device is not connected: "
                                + configuredUdid
                                + ". Connected devices: "
                                + connectedDevices
                );
            }
        });

        List<String> selectedDevices = FrameworkConfig.androidUdid()
                .map(List::of)
                .orElse(connectedDevices);

        int systemPortBase = FrameworkConfig.androidSystemPortBase();

        ensurePortRangeIsValid(
                systemPortBase,
                selectedDevices.size()
        );

        return IntStream.range(0, selectedDevices.size())
                .mapToObj(index -> {
                    String udid = selectedDevices.get(index);
                    return new AndroidDevice(
                            udid,
                            getDeviceDisplayName(udid),
                            systemPortBase + index
                    );
                })
                .toList();
    }

    public static AndroidDevice discoverSingle() {
        List<AndroidDevice> devices = discover();

        if (devices.size() != 1) {
            throw new IllegalStateException(
                    "Expected exactly one Android device, but found "
                            + devices.size()
                            + ": "
                            + devices
                            + ". Set -Dandroid.udid=<UDID> to select one device."
            );
        }

        return devices.get(0);
    }

    private static List<String> getConnectedDeviceUdids() {
        String output = runAdb("devices");

        List<String> connectedDevices = Arrays.stream(output.split("\\R"))
                .skip(1)
                .map(String::trim)
                .filter(line -> line.endsWith("\tdevice"))
                .map(line -> line.split("\\s+")[0])
                .sorted()
                .toList();

        if (connectedDevices.isEmpty()) {
            throw new IllegalStateException(
                    "No authorized Android device is connected. ADB output: "
                            + output
            );
        }

        return connectedDevices;
    }

    private static String getDeviceDisplayName(String udid) {
        String configuredName = runAdb(
                "-s",
                udid,
                "shell",
                "settings",
                "get",
                "global",
                "device_name"
        ).trim();

        if (isUsableDeviceName(configuredName)) {
            return configuredName;
        }

        String model = runAdb(
                "-s",
                udid,
                "shell",
                "getprop",
                "ro.product.model"
        ).trim();

        return isUsableDeviceName(model)
                ? model
                : "Android";
    }

    private static String runAdb(String... arguments) {
        List<String> command = new ArrayList<>();
        command.add(ADB_PATH);
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
                            + ADB_PATH
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

    private static boolean isUsableDeviceName(String value) {
        return value != null
                && !value.isBlank()
                && !"null".equalsIgnoreCase(value);
    }

    private static void ensurePortRangeIsValid(
            int systemPortBase,
            int deviceCount
    ) {
        long lastPort = (long) systemPortBase
                + deviceCount
                - 1L;

        if (lastPort > 65535) {
            throw new IllegalArgumentException(
                    "Not enough Android system ports. Base port: "
                            + systemPortBase
                            + ", device count: "
                            + deviceCount
            );
        }
    }
}
