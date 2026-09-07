package io.github.denofo.mobilee2e.pages.android;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public final class BoardPage extends AndroidBasePage {

    private static final By TOOLBAR_TITLE =
            AppiumBy.id("com.trello:id/toolbar_title");

    private static final By ADD_LIST =
            AppiumBy.id("com.trello:id/add_list_button");

    private static final By LIST_NAME =
            AppiumBy.id("com.trello:id/list_name");

    public BoardPage(AndroidDriver driver) {
        super(driver);
    }

    public BoardPage waitUntilLoaded() {
        visible(TOOLBAR_TITLE);
        visible(ADD_LIST);
        return this;
    }

    public String boardTitle() {
        return visible(TOOLBAR_TITLE).getText();
    }

    public boolean isAddListVisible() {
        return isVisible(ADD_LIST);
    }

    public AddListPage openAddList() {
        click(ADD_LIST);
        return new AddListPage(driver).waitUntilLoaded();
    }

    public BoardPage waitForList(String listName) {
        visibleWithText(LIST_NAME, listName);
        return this;
    }

    public boolean isListVisible(String listName) {
        return isVisibleWithText(LIST_NAME, listName);
    }
}
