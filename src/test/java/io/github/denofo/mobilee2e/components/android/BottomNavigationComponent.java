package io.github.denofo.mobilee2e.components.android;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.github.denofo.mobilee2e.pages.android.AndroidBasePage;
import org.openqa.selenium.By;

public final class BottomNavigationComponent extends AndroidBasePage {

    private static final By BOARDS = AppiumBy.id("com.trello:id/boards");
    private static final By INBOX = AppiumBy.id("com.trello:id/inbox");
    private static final By PLANNER = AppiumBy.id("com.trello:id/planner");
    private static final By ACTIVITY = AppiumBy.id("com.trello:id/activity");
    private static final By ACCOUNT = AppiumBy.id("com.trello:id/account");

    public BottomNavigationComponent(AndroidDriver driver) {
        super(driver);
    }

    public void openBoards() {
        click(BOARDS);
    }

    public void openInbox() {
        click(INBOX);
    }

    public void openPlanner() {
        click(PLANNER);
    }

    public void openActivity() {
        click(ACTIVITY);
    }

    public void openAccount() {
        click(ACCOUNT);
    }
}
