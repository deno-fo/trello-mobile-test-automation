package io.github.denofo.mobilee2e.tests.api;

import io.github.denofo.mobilee2e.api.trello.TrelloRestApi;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.UUID;

import static io.github.denofo.mobilee2e.api.trello.TrelloRestApi.expectStatus;
import static io.restassured.http.Method.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("trello-api")
@Tag("api")
@Tag("integration")
@DisplayName("Live Trello card API (no Appium)")
class ApiCardTest {
    private TrelloRestApi api;
    private String boardId;
    private String sourceList;
    private String destinationList;

    @BeforeEach
    void createIsolatedBoard() {
        api = new TrelloRestApi(); // Missing credentials fail explicitly, not a silent skip.
        Response board = api.send(POST, "boards/", Map.of(
                "name", "API Cards " + UUID.randomUUID(),
                "defaultLists", false, "prefs_permissionLevel", "private"));
        // Register the ID before assertions so teardown can run after setup failures.
        if (board.statusCode() == 200) {
            boardId = board.jsonPath().getString("id");
        }
        expectStatus(board, 200, "create test board");
        assertNotNull(boardId, "Created board must have an ID");
        assertFalse(boardId.isBlank(), "Created board ID must not be blank");
        sourceList = createList("TODO");
        destinationList = createList("Done");
    }

    @AfterEach
    void deleteOwnedBoard() {
        if (boardId != null && !boardId.isBlank()) {
            // Delete only the exact resource created by this test, never search by name.
            expectStatus(api.send(DELETE, "boards/" + boardId, Map.of()), 200,
                    "cleanup test board " + boardId);
        }
    }

    @Test
    @DisplayName("Create card and read persisted fields")
    void createsCard() {
        String name = "API карточка & + " + UUID.randomUUID();
        String id = createCard(name);
        assertCard(getCard(id), id, name, "", sourceList);
    }

    @Test
    @DisplayName("Update title and multiline description without changing card identity")
    void updatesCard() {
        String id = createCard("Original");
        String name = "Updated & + карточка";
        String description = "First line\nОписание: + & =";
        Response updated = expectStatus(api.send(PUT, "cards/" + id,
                Map.of("name", name, "desc", description)), 200, "update card");
        assertCard(updated, id, name, description, sourceList);
        assertCard(getCard(id), id, name, description, sourceList);
    }

    @Test
    @DisplayName("Move same card to another list; remove it from the source")
    void movesCard() {
        String id = createCard("Move me");
        Response moved = expectStatus(api.send(PUT, "cards/" + id,
                Map.of("idList", destinationList)), 200, "move card");
        assertCard(moved, id, "Move me", "", destinationList);
        assertCard(getCard(id), id, "Move me", "", destinationList);
        assertFalse(listCards(sourceList).jsonPath().getList("id", String.class).contains(id));
        assertTrue(listCards(destinationList).jsonPath().getList("id", String.class).contains(id));
    }

    @Test
    @DisplayName("Delete card; subsequent read returns 404")
    void deletesCard() {
        String id = createCard("Delete me");
        expectStatus(api.send(DELETE, "cards/" + id, Map.of()), 200, "delete card");
        expectStatus(api.send(GET, "cards/" + id, Map.of()), 404, "read deleted card");
        assertFalse(listCards(sourceList).jsonPath().getList("id", String.class).contains(id));
    }

    @Test
    @DisplayName("Reject malformed destination list and preserve original card")
    void rejectsInvalidList() {
        String id = createCard("Keep me");
        expectStatus(api.send(PUT, "cards/" + id,
                Map.of("idList", "not-a-trello-id")), 400, "reject invalid list");
        assertCard(getCard(id), id, "Keep me", "", sourceList);
    }

    @Test
    @DisplayName("Private card cannot be read without authorization")
    void rejectsUnauthenticatedRead() {
        String id = createCard("Private");
        expectStatus(api.withoutAuthorization(GET, "cards/" + id), 401,
                "reject unauthenticated read");
        assertCard(getCard(id), id, "Private", "", sourceList);
    }

    private String createList(String name) {
        String id = expectStatus(api.send(POST, "lists",
                Map.of("idBoard", boardId, "name", name)), 200, "create list")
                .jsonPath().getString("id");
        assertNotNull(id, "Created list ID");
        return id;
    }

    private String createCard(String name) {
        Response created = expectStatus(api.send(POST, "cards",
                Map.of("idList", sourceList, "name", name)), 200, "create card");
        String id = created.jsonPath().getString("id");
        assertNotNull(id, "Created card ID");
        assertTrue(id.matches("[0-9a-f]{24}"), "Created card must have a Trello ID");
        assertCard(created, id, name, "", sourceList);
        return id;
    }

    private Response getCard(String id) {
        return expectStatus(api.send(GET, "cards/" + id, Map.of()), 200, "get card");
    }

    private Response listCards(String listId) {
        return expectStatus(api.send(GET, "lists/" + listId + "/cards", Map.of()),
                200, "list cards");
    }

    private void assertCard(Response response, String id, String name, String desc, String listId) {
        assertTrue(response.contentType().startsWith("application/json"), "JSON response expected");
        var json = response.jsonPath();
        assertAll(
                () -> assertEquals(id, json.getString("id")),
                () -> assertEquals(name, json.getString("name")),
                () -> assertEquals(desc, json.getString("desc")),
                () -> assertEquals(listId, json.getString("idList")),
                () -> assertEquals(boardId, json.getString("idBoard")),
                () -> assertEquals(Boolean.FALSE, json.getBoolean("closed")));
    }
}
