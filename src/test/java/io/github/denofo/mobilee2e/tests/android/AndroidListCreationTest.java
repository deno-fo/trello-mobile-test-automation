package io.github.denofo.mobilee2e.tests.android;

import io.github.denofo.mobilee2e.api.trello.TrelloBoardCleanup;
import io.github.denofo.mobilee2e.api.trello.TrelloApiClient;
import io.github.denofo.mobilee2e.api.trello.TrelloBoard;
import io.github.denofo.mobilee2e.junit.AndroidDeviceTest;
import io.github.denofo.mobilee2e.pages.android.BoardPage;
import io.github.denofo.mobilee2e.pages.android.BoardsPage;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AndroidListCreationTest extends BaseAndroidTest {

    @AndroidDeviceTest
    void listShouldBeCreatedOnBoard() {
        String suffix = Long.toString(Instant.now().toEpochMilli());
        String boardName = "e2e-board-" + suffix;
        String listName = "TODO-" + suffix;

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
                .waitForList(listName);

        TrelloApiClient apiClient = new TrelloApiClient();
        TrelloBoard board = apiClient.findOpenBoardByName(boardName)
                .orElseThrow(
                        () -> new AssertionError(
                                "Created board was not found through Trello API."
                        )
                );

        assertTrue(
                boardPage.isListVisible(listName),
                "Created list is not visible on the board."
        );
        assertTrue(
                apiClient.findOpenListByName(board.id(), listName).isPresent(),
                "Created list was not found through Trello API."
        );
    }
}
