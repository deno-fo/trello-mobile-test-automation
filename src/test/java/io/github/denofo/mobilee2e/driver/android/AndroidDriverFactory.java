package io.github.denofo.mobilee2e.driver.android;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.github.denofo.mobilee2e.config.FrameworkConfig;
import io.github.denofo.mobilee2e.device.android.AndroidDevice;
import io.github.denofo.mobilee2e.server.AppiumServer;

import java.net.MalformedURLException;
import java.net.URL;

public final class AndroidDriverFactory {

    private AndroidDriverFactory() {
    }

    public static AndroidDriver create(
            AndroidDevice device
    ) {
        AppiumServer.ensureAvailable(
                FrameworkConfig.appiumServerUri()
        );

        UiAutomator2Options options =
                AndroidCapabilitiesFactory.create(device);

        try {
            URL serverUrl =
                    FrameworkConfig.appiumServerUri().toURL();

            return new AndroidDriver(
                    serverUrl,
                    options
            );
        } catch (MalformedURLException exception) {
            throw new IllegalStateException(
                    "Invalid Appium server URL: "
                            + FrameworkConfig.appiumServerUri(),
                    exception
            );
        }
    }
}
