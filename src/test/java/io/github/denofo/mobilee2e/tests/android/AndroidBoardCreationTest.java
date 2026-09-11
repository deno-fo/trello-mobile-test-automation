package io.github.denofo.mobilee2e.tests.android;

import io.github.denofo.mobilee2e.api.trello.TrelloBoardCleanup;
import io.github.denofo.mobilee2e.junit.AndroidDeviceTest;
import io.github.denofo.mobilee2e.pages.android.BoardPage;
import io.github.denofo.mobilee2e.pages.android.BoardsPage;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AndroidBoardCreationTest extends BaseAndroidTest {

    @AndroidDeviceTest
    void boardShouldBeCreatedAndOpened() {
        String boardName =
                "e2e-board-" + Instant.now().toEpochMilli();

        registerAfterSessionCleanup(
                new TrelloBoardCleanup(boardName)
        );

        BoardPage boardPage = new BoardsPage(driver)
                .waitUntilLoaded()

                .openCreateBoard()
                .enterBoardName(boardName)
                .createBoard();

        assertAll(
                () -> assertEquals(
                        boardName,
                        boardPage.boardTitle(),
                        "Created board title does not match."
                ),
                () -> assertTrue(
                        boardPage.isAddListVisible(),
                        "Add list button is not visible on the created board."
                )
        );
    }
}
