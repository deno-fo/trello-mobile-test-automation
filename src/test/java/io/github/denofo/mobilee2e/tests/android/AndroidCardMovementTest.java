package io.github.denofo.mobilee2e.tests.android;

import io.github.denofo.mobilee2e.api.trello.TrelloApiClient;
import io.github.denofo.mobilee2e.config.FrameworkConfig;
import io.github.denofo.mobilee2e.junit.AndroidDeviceTest;
import io.github.denofo.mobilee2e.pages.android.BoardsPage;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AndroidCardMovementTest extends BaseAndroidTest {

    @AndroidDeviceTest
    void cardShouldMoveFromTodoToDone() {
        TrelloApiClient api = new TrelloApiClient();
        String suffix = UUID.randomUUID().toString();
        var board = api.createBoard("e2e-move-" + suffix);
        registerAfterSessionCleanup(() -> api.deleteBoard(board.id()));
        var todo = api.createList(board.id(), "TODO");
        var done = api.createList(board.id(), "Done");
        var card = api.createCard(todo.id(), "move-card-" + suffix);

        // Refresh app state after API setup without clearing the authenticated account.
        String appPackage = FrameworkConfig.androidAppPackage();
        driver.terminateApp(appPackage);
        driver.activateApp(appPackage);

        var details = new BoardsPage(driver).waitUntilLoaded().openBoard(board.name())
                .waitForCard(card.name()).openCard(card.name());
        details.openMove().selectList(done.name()).confirmMove();

        String expectedLocation = "Viewing card on board " + board.name() + " in list " + done.name();
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ignored -> expectedLocation.equals(details.boardAndListDescription()));
        assertEquals(card.name(), details.name(), "Moving should preserve the card name.");

        var moved = api.awaitCardByName(done.id(), card.name());
        assertEquals(card.id(), moved.id(), "The original card must move, not a copy.");
        assertEquals(done.id(), moved.idList(), "API should return the destination list id.");
        assertTrue(api.getCards(todo.id()).stream().noneMatch(item -> card.id().equals(item.id())),
                "The original list should no longer contain the card.");
    }
}
