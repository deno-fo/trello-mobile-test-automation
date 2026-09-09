package io.github.denofo.mobilee2e.pages.android;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public final class AddCardPage extends AndroidBasePage {

    private static final By CARD_NAME =
            AppiumBy.id("com.trello:id/card_name_edit_text");

    private static final By CONFIRM =
            AppiumBy.id("com.trello:id/confirm");

    public AddCardPage(AndroidDriver driver) {
        super(driver);
    }

    public AddCardPage waitUntilLoaded() {
        visible(CARD_NAME);
        visible(CONFIRM);
        return this;
    }

    public AddCardPage enterCardName(String cardName) {
        type(CARD_NAME, cardName);
        return this;
    }

    public BoardPage createCard() {
        click(CONFIRM);
        return new BoardPage(driver);
    }
}
