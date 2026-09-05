package io.github.denofo.mobilee2e.pages.android;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public final class CreateBoardPage extends AndroidBasePage {

    private static final By BOARD_NAME = AppiumBy.id("com.trello:id/board_name");
    private static final By WORKSPACE = AppiumBy.id("com.trello:id/org_spinner");
    private static final By VISIBILITY = AppiumBy.id("com.trello:id/visibility_spinner");
    private static final By BACKGROUND = AppiumBy.id("com.trello:id/background_row");
    private static final By CREATE_BOARD = AppiumBy.id("com.trello:id/create_board_button");

    public CreateBoardPage(AndroidDriver driver) {
        super(driver);
    }

    public CreateBoardPage waitUntilLoaded() {
        visible(BOARD_NAME);
        visible(CREATE_BOARD);
        return this;
    }

    public CreateBoardPage enterBoardName(String name) {
        type(BOARD_NAME, name);
        return this;
    }

    public BoardPage createBoard() {
        click(CREATE_BOARD);
        return new BoardPage(driver).waitUntilLoaded();
    }

    public boolean isWorkspaceSelectorVisible() {
        return isVisible(WORKSPACE);
    }

    public boolean isVisibilitySelectorVisible() {
        return isVisible(VISIBILITY);
    }

    public boolean isBackgroundSelectorVisible() {
        return isVisible(BACKGROUND);
    }
}
