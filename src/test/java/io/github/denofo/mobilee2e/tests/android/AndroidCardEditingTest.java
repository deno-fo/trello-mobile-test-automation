package io.github.denofo.mobilee2e.tests.android;

import io.github.denofo.mobilee2e.api.trello.TrelloApiClient;
import io.github.denofo.mobilee2e.api.trello.TrelloBoardCleanup;
import io.github.denofo.mobilee2e.junit.AndroidDeviceTest;
import io.github.denofo.mobilee2e.pages.android.BoardPage;
import io.github.denofo.mobilee2e.pages.android.BoardsPage;
import io.github.denofo.mobilee2e.pages.android.CardDetailsPage;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AndroidCardEditingTest extends BaseAndroidTest {

    @AndroidDeviceTest
    void editedNameAndDescriptionShouldPersistInUiAndApi() {
        String suffix = UUID.randomUUID().toString();
        String boardName = "e2e-edit-board-" + suffix;
        String listName = "TODO-" + suffix;
        String originalName = "e2e-card-" + suffix;
        String updatedName = "edited-card-" + suffix;
        String description = "Description updated through Android UI " + suffix;

        registerAfterSessionCleanup(new TrelloBoardCleanup(boardName));

        BoardPage boardPage = new BoardsPage(driver).waitUntilLoaded()
                .openCreateBoard().enterBoardName(boardName).createBoard();
        boardPage.openAddList().enterListName(listName).createList()
                .waitForList(listName).openAddCard().enterCardName(originalName)
                .createCard().waitForCard(originalName);

        TrelloApiClient api = new TrelloApiClient();
        var board = api.findOpenBoardByName(boardName)
                .orElseThrow(() -> new AssertionError("Test board not found through API."));
        var list = api.awaitOpenListByName(board.id(), listName);
        var originalCard = api.awaitCardByName(list.id(), originalName);

        CardDetailsPage reopened = boardPage.openCard(originalName)
                .rename(updatedName).setDescription(description)
                .backToBoard().waitForCard(updatedName).openCard(updatedName);

        assertEquals(updatedName, reopened.name(), "Card name should persist after reopening.");
        assertEquals(description, reopened.descriptionInEditor(),
                "Card description should persist after reopening.");

        var updatedCard = api.getCards(list.id()).stream()
                .filter(card -> originalCard.id().equals(card.id()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Edited card not found through API."));
        assertEquals(updatedName, updatedCard.name(), "API should return the updated name.");
        assertEquals(description, updatedCard.desc(), "API should return the updated description.");
    }
}
