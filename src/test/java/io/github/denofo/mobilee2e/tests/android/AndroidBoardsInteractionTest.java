package io.github.denofo.mobilee2e.tests.android;

import io.github.denofo.mobilee2e.junit.AndroidDeviceTest;
import io.github.denofo.mobilee2e.pages.android.BoardsPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AndroidBoardsInteractionTest extends BaseAndroidTest {

    @AndroidDeviceTest
    void quickAddShouldOpenAndCancelWithoutCreatingCard() {
        BoardsPage boardsPage = new BoardsPage(driver)
                .waitUntilLoaded();

        boardsPage.openQuickAdd();
        assertTrue(
                boardsPage.isQuickAddEditorVisible(),
                "Quick Add editor should be visible after opening it."
        );

        boardsPage.cancelQuickAdd();
        assertTrue(
                boardsPage.isQuickAddCollapsedVisible(),
                "Quick Add should return to collapsed state after cancellation."
        );
    }
}
