package io.github.denofo.mobilee2e.pages.android;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public final class MoveCardPage extends AndroidBasePage {

    private static final By TITLE = By.xpath("//android.widget.TextView[@text='Move card']");
    // Anchor to the field label observed in Inspector, not a global View instance number.
    private static final By LIST_FIELD = By.xpath(
            "//android.widget.ScrollView/android.view.View"
                    + "[android.widget.TextView[@text='List']]");
    private static final By CONFIRM = AppiumBy.accessibilityId("Confirm");
    private static final By OPTION_TEXT = AppiumBy.className("android.widget.TextView");

    public MoveCardPage(AndroidDriver driver) {
        super(driver);
    }

    public MoveCardPage waitUntilLoaded() {
        visible(TITLE);
        visible(LIST_FIELD);
        return this;
    }

    public MoveCardPage selectList(String listName) {
        click(LIST_FIELD);
        visibleWithText(OPTION_TEXT, listName).click();
        return this;
    }

    public CardDetailsPage confirmMove() {
        click(CONFIRM);
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.invisibilityOfElementLocated(TITLE));
        return new CardDetailsPage(driver).waitUntilLoaded();
    }
}
