package io.github.denofo.mobilee2e.tests.android;

import io.github.denofo.mobilee2e.api.trello.TrelloApiClient;
import io.github.denofo.mobilee2e.api.trello.TrelloBoard;
import io.github.denofo.mobilee2e.api.trello.TrelloBoardCleanup;
import io.github.denofo.mobilee2e.api.trello.TrelloList;
import io.github.denofo.mobilee2e.junit.AndroidDeviceTest;
import io.github.denofo.mobilee2e.pages.android.BoardPage;
import io.github.denofo.mobilee2e.pages.android.BoardsPage;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AndroidCardCreationTest extends BaseAndroidTest {

    @AndroidDeviceTest
    void cardShouldBeCreatedInListAndVisibleThroughApi() {
        String suffix = Long.toString(Instant.now().toEpochMilli());
        String boardName = "e2e-board-" + suffix;
        String listName = "TODO-" + suffix;
        String cardName = "e2e-card-" + suffix;

        registerAfterSessionCleanup(
                new TrelloBoardCleanup(boardName)
        );

        BoardPage boardPage = new BoardsPage(driver)
                .waitUntilLoaded()
                .openCreateBoard()
                .enterBoardName(boardName)
                .createBoard();

        boardPage.openAddList()
                .enterListName(listName)
                .createList()
                .waitForList(listName)
                .openAddCard()
                .enterCardName(cardName)
                .createCard()
                .waitForCard(cardName);

        TrelloApiClient apiClient = new TrelloApiClient();
        TrelloBoard board = apiClient.findOpenBoardByName(boardName)
                .orElseThrow(
                        () -> new AssertionError(
                                "Created board was not found through Trello API."
                        )
                );
        TrelloList list = apiClient.findOpenListByName(board.id(), listName)
                .orElseThrow(
                        () -> new AssertionError(
                                "Created list was not found through Trello API."
                        )
                );

        assertTrue(
                boardPage.isCardVisible(cardName),
                "Created card is not visible in the list."
        );
        assertTrue(
                apiClient.findCardByName(list.id(), cardName).isPresent(),
                "Created card was not found through Trello API."
        );
    }
}
