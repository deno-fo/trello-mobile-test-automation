package io.github.denofo.mobilee2e.pages.android;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public final class AddListPage extends AndroidBasePage {

    private static final By LIST_NAME =
            AppiumBy.id("com.trello:id/list_name_edit_text");

    private static final By CONFIRM =
            AppiumBy.id("com.trello:id/confirm");

    public AddListPage(AndroidDriver driver) {
        super(driver);
    }

    public AddListPage waitUntilLoaded() {
        visible(LIST_NAME);
        visible(CONFIRM);
        return this;
    }

    public AddListPage enterListName(String listName) {
        type(LIST_NAME, listName);
        return this;
    }

    public BoardPage createList() {
        click(CONFIRM);
        return new BoardPage(driver);
    }
}
