package io.github.denofo.mobilee2e.device.android;

import io.github.denofo.mobilee2e.config.FrameworkConfig;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class AndroidDeviceProvider {

    private static final AdbClient ADB = new AdbClient();

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
        String output = ADB.run("devices");

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
        String configuredName = ADB.run(
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

        String model = ADB.run(
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
