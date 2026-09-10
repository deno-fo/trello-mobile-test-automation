package io.github.denofo.mobilee2e.tests.android;

import io.appium.java_client.android.AndroidDriver;
import io.github.denofo.mobilee2e.app.android.AndroidAppState;
import io.github.denofo.mobilee2e.config.FrameworkConfig;
import io.github.denofo.mobilee2e.device.android.AndroidDevice;
import io.github.denofo.mobilee2e.device.android.AndroidDeviceContext;
import io.github.denofo.mobilee2e.device.android.AndroidDeviceProvider;
import io.github.denofo.mobilee2e.diagnostics.FailureArtifacts;
import io.github.denofo.mobilee2e.driver.android.AndroidDriverFactory;
import io.github.denofo.mobilee2e.junit.FailureArtifactsExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriverException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

@ExtendWith(FailureArtifactsExtension.class)
public abstract class BaseAndroidTest implements FailureArtifactsExtension.Source {

    protected AndroidDriver driver;
    protected AndroidDevice device;

    private final Deque<AutoCloseable> afterSessionCleanups =
            new ArrayDeque<>();

    @Override
    public void captureFailureArtifacts(String label) throws IOException {
        if (driver == null) {
            System.err.println("Failure artifacts unavailable: no Appium driver was created.");
            return;
        }
        String deviceLabel = device == null ? "unknown-device" : device.udid();
        FailureArtifacts.capture(Path.of("artifacts", "android"),
                deviceLabel + "-" + label,
                () -> driver.getScreenshotAs(OutputType.BYTES), driver::getPageSource);
    }

    @BeforeEach
    public void setUp() {
        device = resolveDevice();

        AndroidAppState.ensureInstalled(device);

        driver = AndroidDriverFactory.create(device);
    }

    protected void resetAppData() {
        closeAppSession();

        AndroidAppState.clearData(device);
        driver = AndroidDriverFactory.create(device);
    }

    protected void registerAfterSessionCleanup(
            AutoCloseable cleanup
    ) {
        afterSessionCleanups.push(
                Objects.requireNonNull(cleanup, "cleanup")
        );
    }

    protected void closeAppSession() {
        if (driver == null) {
            return;
        }

        try {
            driver.terminateApp(
                    FrameworkConfig.androidAppPackage()
            );
        } catch (WebDriverException ignored) {
            // The app/session may already be gone after a driver/UIA2 failure.
        }

        try {
            driver.quit();
        } catch (WebDriverException ignored) {
            // The Appium session may already be gone after a driver/UIA2 failure.
        } finally {
            driver = null;
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeAppSession();

        Exception cleanupFailure = null;

        while (!afterSessionCleanups.isEmpty()) {
            try {
                afterSessionCleanups.pop().close();
            } catch (Exception exception) {
                if (cleanupFailure == null) {
                    cleanupFailure = exception;
                } else {
                    cleanupFailure.addSuppressed(exception);
                }
            }
        }

        if (cleanupFailure != null) {
            throw cleanupFailure;
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
