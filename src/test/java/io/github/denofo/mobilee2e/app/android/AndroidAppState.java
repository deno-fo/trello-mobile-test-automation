package io.github.denofo.mobilee2e.app.android;

import io.github.denofo.mobilee2e.config.FrameworkConfig;
import io.github.denofo.mobilee2e.device.android.AdbClient;
import io.github.denofo.mobilee2e.device.android.AndroidDevice;

public final class AndroidAppState {

    private static final AdbClient ADB = new AdbClient();

    private AndroidAppState() {
    }

    public static void ensureInstalled(
            AndroidDevice device
    ) {
        String packageName = FrameworkConfig.androidAppPackage();

        if (ADB.isPackageInstalled(
                device.udid(),
                packageName
        )) {
            return;
        }

        throw new IllegalStateException(
                "Application is not installed on Android device "
                        + device.displayName()
                        + ". Expected package: "
                        + packageName
        );
    }

    public static void clearData(
            AndroidDevice device
    ) {
        ADB.clearAppData(
                device.udid(),
                FrameworkConfig.androidAppPackage()
        );
    }
}
