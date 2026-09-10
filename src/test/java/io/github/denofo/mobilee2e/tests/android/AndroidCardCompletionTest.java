package io.github.denofo.mobilee2e.tests.android;

import io.github.denofo.mobilee2e.api.trello.TrelloApiClient;
import io.github.denofo.mobilee2e.config.FrameworkConfig;
import io.github.denofo.mobilee2e.junit.AndroidDeviceTest;
import io.github.denofo.mobilee2e.pages.android.BoardsPage;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AndroidCardCompletionTest extends BaseAndroidTest {

    @AndroidDeviceTest
    void cardShouldBeMarkedCompleteThroughUiAndApi() {
        TrelloApiClient api = new TrelloApiClient();
        String suffix = UUID.randomUUID().toString();
        var board = api.createBoard("e2e-complete-" + suffix);
        registerAfterSessionCleanup(() -> api.deleteBoard(board.id()));
        var list = api.createList(board.id(), "TODO");
        var card = api.createCard(list.id(), "complete-card-" + suffix);

        String appPackage = FrameworkConfig.androidAppPackage();
        driver.terminateApp(appPackage);
        driver.activateApp(appPackage);

        var details = new BoardsPage(driver).waitUntilLoaded().openBoard(board.name())
                .waitForCard(card.name()).openCard(card.name())
                .markComplete();
        assertTrue(details.isComplete(), "Card should be marked complete in the UI.");

        var completed = api.getCard(card.id());
        assertEquals(card.id(), completed.id(), "The original card must be updated.");
        assertTrue(completed.closed(), "API should report the card as closed.");
    }
}
