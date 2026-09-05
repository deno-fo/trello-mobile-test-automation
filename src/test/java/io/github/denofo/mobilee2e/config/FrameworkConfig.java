package io.github.denofo.mobilee2e.config;

import java.net.URI;
import java.util.Optional;

public final class FrameworkConfig {

    private static final String DEFAULT_APPIUM_URL =
            "http://127.0.0.1:4723";

    private static final int DEFAULT_ANDROID_SYSTEM_PORT_BASE =
            8200;

    private FrameworkConfig() {
    }

    public static URI appiumServerUri() {
        return URI.create(
                ConfigResolver.optional(
                        "appium.url",
                        "APPIUM_URL"
                ).orElse(DEFAULT_APPIUM_URL)
        );
    }

    public static Optional<String> androidUdid() {
        return ConfigResolver.optional(
                "android.udid",
                "ANDROID_UDID"
        );
    }

    public static int androidSystemPortBase() {
        String rawValue = ConfigResolver.optional(
                "android.systemPortBase",
                "ANDROID_SYSTEM_PORT_BASE"
        ).orElse(
                Integer.toString(DEFAULT_ANDROID_SYSTEM_PORT_BASE)
        );

        try {
            int port = Integer.parseInt(rawValue);

            if (port < 1024 || port > 65535) {
                throw new IllegalArgumentException(
                        "Android system port base must be between 1024 and 65535: "
                                + port
                );
            }

            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Android system port base must be a number: "
                            + rawValue,
                    exception
            );
        }
    }

    public static Optional<String> iosUdid() {
        return ConfigResolver.optional(
                "ios.udid",
                "IOS_UDID"
        );
    }

    public static String androidAppPackage() {
        return ConfigResolver.required(
                "android.appPackage",
                "ANDROID_APP_PACKAGE"
        );
    }

    public static String androidAppActivity() {
        return ConfigResolver.required(
                "android.appActivity",
                "ANDROID_APP_ACTIVITY"
        );
    }

    public static String iosBundleId() {
        return ConfigResolver.required(
                "ios.bundleId",
                "IOS_BUNDLE_ID"
        );
    }

    public static String iosTeamId() {
        return ConfigResolver.required(
                "ios.teamId",
                "IOS_TEAM_ID"
        );
    }

    public static String iosWdaBundleId() {
        return ConfigResolver.required(
                "ios.wdaBundleId",
                "IOS_WDA_BUNDLE_ID"
        );
    }
}
