package io.github.denofo.mobilee2e.tests.android;

import io.appium.java_client.android.AndroidDriver;
import io.github.denofo.mobilee2e.app.android.AndroidAppState;
import io.github.denofo.mobilee2e.device.android.AndroidDevice;
import io.github.denofo.mobilee2e.device.android.AndroidDeviceContext;
import io.github.denofo.mobilee2e.device.android.AndroidDeviceProvider;
import io.github.denofo.mobilee2e.driver.android.AndroidDriverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriverException;

public abstract class BaseAndroidTest {

    protected AndroidDriver driver;
    protected AndroidDevice device;

    @BeforeEach
    public void setUp() {
        device = resolveDevice();

        AndroidAppState.ensureInstalled(device);

        driver = AndroidDriverFactory.create(device);
    }

    protected void resetAppData() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }

        AndroidAppState.clearData(device);
        driver = AndroidDriverFactory.create(device);
    }

    @AfterEach
    public void tearDown() {
        if (driver == null) {
            return;
        }

        try {
            driver.quit();
        } catch (WebDriverException ignored) {
            // The Appium session may already be gone after a driver/WDA/UIA2 failure.
        } finally {
            driver = null;
        }
    }

    private AndroidDevice resolveDevice() {
        AndroidDevice assignedDevice = AndroidDeviceContext.get();

        if (assignedDevice != null) {
            return assignedDevice;
        }

        return AndroidDeviceProvider.discoverSingle();
    }
}
