package io.github.denofo.mobilee2e.pages.android;

import io.appium.java_client.android.AndroidDriver;
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
        // Saving an edit may leave the keyboard open; Back must close the card.
        if (driver.isKeyboardShown()) {
            driver.hideKeyboard();
        }
        driver.navigate().back();
        return new BoardPage(driver).waitUntilLoaded();
    }
}
