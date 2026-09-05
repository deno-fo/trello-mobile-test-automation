package io.github.denofo.mobilee2e.tests.android;

import io.github.denofo.mobilee2e.config.FrameworkConfig;
import io.github.denofo.mobilee2e.junit.AndroidDeviceTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AndroidAppLaunchTest extends BaseAndroidTest {

    @AndroidDeviceTest
    void appShouldLaunch() {
        assertEquals(
                FrameworkConfig.androidAppPackage(),
                driver.getCurrentPackage(),
                "Unexpected Android package is in the foreground."
        );
    }
}
