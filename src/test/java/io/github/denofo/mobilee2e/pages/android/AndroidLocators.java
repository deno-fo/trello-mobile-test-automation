package io.github.denofo.mobilee2e.pages.android;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

public final class AndroidLocators {

    private AndroidLocators() {
    }

    public static By rawResourceId(String resourceId) {
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException(
                    "Android resource id must not be blank."
            );
        }

        return AppiumBy.androidUIAutomator(
                "new UiSelector().resourceId(\""
                        + resourceId
                        + "\")"
        );
    }
}
