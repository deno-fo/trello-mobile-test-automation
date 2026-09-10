package io.github.denofo.mobilee2e.pages.android;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

import static io.github.denofo.mobilee2e.pages.android.AndroidLocators.rawResourceId;

public final class CardDetailsPage extends AndroidBasePage {

    private static final By NAME = rawResourceId("CardName");
    private static final By DESCRIPTION_SECTION = rawResourceId("DescriptionSection");
    private static final By DESCRIPTION_INPUT = By.xpath(
            "//*[@resource-id='DescriptionSection']"
                    + "//android.widget.EditText[@resource-id='mentionComposeTextField']"
    );
    private static final By SAVE = rawResourceId("SubmitIcon");
    private static final By BOARD_LIST_INFORMATION = rawResourceId("boardListInformation");
    private static final By CLOSE = AppiumBy.accessibilityId("Close");
    private static final By DONE = rawResourceId("doneCheckbox");

    public CardDetailsPage(AndroidDriver driver) {
        super(driver);
    }

    public CardDetailsPage waitUntilLoaded() {
        visible(NAME);
        return this;
    }

    public CardDetailsPage rename(String name) {
        click(NAME);
        type(NAME, name);
        click(SAVE);
        return this;
    }

    public CardDetailsPage setDescription(String description) {
        click(DESCRIPTION_SECTION);
        type(DESCRIPTION_INPUT, description);
        click(SAVE);
        return this;
    }

    public String name() {
        return visible(NAME).getText();
    }

    public CardDetailsPage markComplete() {
        if (!isComplete()) {
            click(DONE);
        }
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10))
                .until(ignored -> isComplete());
        return this;
    }

    public boolean isComplete() {
        return Boolean.parseBoolean(visible(DONE).getAttribute("checked"));
    }

    public MoveCardPage openMove() {
        click(BOARD_LIST_INFORMATION);
        return new MoveCardPage(driver).waitUntilLoaded();
    }

    public String boardAndListDescription() {
        return visible(BOARD_LIST_INFORMATION).getAttribute("content-desc");
    }

    public String descriptionInEditor() {
        click(DESCRIPTION_SECTION);
        return visible(DESCRIPTION_INPUT).getText();
    }

    public BoardPage backToBoard() {
        // Close the card through its own toolbar action instead of relying on system Back.
        if (driver.isKeyboardShown()) {
            driver.hideKeyboard();
        }
        click(CLOSE);
        return new BoardPage(driver).waitUntilLoaded();
    }

    public BoardPage backToBoard(String boardName) {
        if (driver.isKeyboardShown()) {
            driver.hideKeyboard();
        }
        click(CLOSE);

        By toolbarTitle = AppiumBy.id("com.trello:id/toolbar_title");
        boolean boardToolbarVisible = driver.findElements(toolbarTitle).stream()
                .anyMatch(element -> {
                    try {
                        return element.isDisplayed();
                    } catch (RuntimeException ignored) {
                        return false;
                    }
                });
        if (boardToolbarVisible) {
            return new BoardPage(driver).waitUntilLoaded();
        }

        return new BoardsPage(driver).waitUntilLoaded().openBoard(boardName);
    }
}
