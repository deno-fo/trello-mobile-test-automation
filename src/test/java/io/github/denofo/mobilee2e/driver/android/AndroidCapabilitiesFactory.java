package io.github.denofo.mobilee2e.driver.android;

import io.appium.java_client.android.options.UiAutomator2Options;
import io.github.denofo.mobilee2e.config.FrameworkConfig;
import io.github.denofo.mobilee2e.device.android.AndroidDevice;

public final class AndroidCapabilitiesFactory {

    private AndroidCapabilitiesFactory() {
    }

    public static UiAutomator2Options create(
            AndroidDevice device
    ) {
        UiAutomator2Options options = new UiAutomator2Options();

        options.setUdid(device.udid());
        options.setAppPackage(FrameworkConfig.androidAppPackage());
        options.setAppActivity(FrameworkConfig.androidAppActivity());
        options.setNoReset(true);
        options.setCapability("appium:systemPort", device.systemPort());
        options.setCapability("appium:forceAppLaunch", true);

        return options;
    }
}
