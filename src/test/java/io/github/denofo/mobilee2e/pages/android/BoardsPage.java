package io.github.denofo.mobilee2e.pages.android;

import io.appium.java_client.android.AndroidDriver;
import io.github.denofo.mobilee2e.components.android.BottomNavigationComponent;
import org.openqa.selenium.By;

public final class BoardsPage extends AndroidBasePage {

    private static final By SEARCH = AndroidLocators.rawResourceId("search");
    private static final By CREATE_BOARD = AndroidLocators.rawResourceId("homeBoardsFab");
    private static final By QUICK_ADD = AndroidLocators.rawResourceId("QuickAddInlineInput");
    private static final By QUICK_ADD_INPUT = AndroidLocators.rawResourceId("InboxInputBasicTextField");
    private static final By SUBMIT = AndroidLocators.rawResourceId("SubmitIcon");
    private static final By CANCEL = AndroidLocators.rawResourceId("CancelIcon");

    private final BottomNavigationComponent bottomNavigation;

    public BoardsPage(AndroidDriver driver) {
        super(driver);
        this.bottomNavigation = new BottomNavigationComponent(driver);
    }

    public BoardsPage waitUntilLoaded() {
        visible(QUICK_ADD);
        return this;
    }

    public BottomNavigationComponent bottomNavigation() {
        return bottomNavigation;
    }

    public void openSearch() {
        click(SEARCH);
    }

    public void openCreateBoard() {
        click(CREATE_BOARD);
    }

    public void openQuickAdd() {
        click(QUICK_ADD);
        visible(QUICK_ADD_INPUT);
    }

    public void typeQuickAddTitle(String title) {
        type(QUICK_ADD_INPUT, title);
    }

    public void submitQuickAdd() {
        click(SUBMIT);
    }

    public void cancelQuickAdd() {
        click(CANCEL);
        visible(QUICK_ADD);
    }

    public boolean isQuickAddEditorVisible() {
        return isVisible(QUICK_ADD_INPUT);
    }

    public boolean isQuickAddCollapsedVisible() {
        return isVisible(QUICK_ADD);
    }
}
