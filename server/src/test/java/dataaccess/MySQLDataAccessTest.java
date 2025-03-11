package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MySQLDataAccessTest {
    private MySQLDataAccess dataAccess;
    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MySQLDataAccess();
        dataAccess.clear();
    }

    @Test
    void clearSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("test", "password", "test@email.com"));
        dataAccess.clear();
        assertNull(dataAccess.getUser("user"));
    }

    @Test
    void createGameIDSuccess() throws DataAccessException {
        int id = dataAccess.createGameID();
        assertTrue(id > 0);
    }

    @Test
    void createUserSuccess() throws DataAccessException {
        UserData user = new UserData("test", "password", "test@email.com");
        dataAccess.createUser(user);
        UserData user2 = dataAccess.getUser("user");
        assertNotNull(user2);
        assertEquals("test", user2.username());
    }

    @Test
    void createDuplicateUserFailure() throws DataAccessException {
        UserData user = new UserData("test", "password", "test@email.com");
        assertDoesNotThrow(() -> dataAccess.createUser(user));
        assertThrows(DataAccessException.class, () -> dataAccess.createUser(user));
    }

    @Test
    void getUserSuccess() throws DataAccessException {
        UserData user = new UserData("test", "password", "test@email.com");
        dataAccess.createUser(user);
        UserData user2 = dataAccess.getUser("user");
        assertNotNull(user2);
        assertEquals("test", user2.username());
    }

    @Test
    void getNonexistentUserFailure() throws DataAccessException {
        assertNull(dataAccess.getUser("nonexistent"));
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        GameData game = new GameData(1, null, null, "testGame", new ChessGame());
        dataAccess.createGame(game);
        GameData game2 = dataAccess.getGame(1);
        assertNotNull(game2);
        assertEquals("testGame", game2.gameName());
    }

    @Test
    void createDuplicateGameFailure() throws DataAccessException {
        GameData game = new GameData(1, null, null, "testGame", new ChessGame());
        dataAccess.createGame(game);
        assertThrows(DataAccessException.class, () -> dataAccess.createGame(game));
    }

    @Test
    void getGameSuccess() throws DataAccessException {
        GameData game = new GameData(1, null, null, "testGame", new ChessGame());
        dataAccess.createGame(game);
        GameData game2 = dataAccess.getGame(1);
        assertNotNull(game2);
        assertEquals("testGame", game2.gameName());    }

    @Test
    void getNonExistentGameFailure() throws DataAccessException {
        assertNull(dataAccess.getGame(999));
    }

    @Test
    void listOfGamesSuccess() throws DataAccessException {
        GameData game = new GameData(1, null, null, "testGame", new ChessGame());
        dataAccess.createGame(game);
        GameData game2 = new GameData(2, null, null, "testGame2", new ChessGame());
        dataAccess.createGame(game2);
        var games = dataAccess.listOfGames();
        assertEquals(2, games.size());
    }

    @Test
    void listOfGamesIsEmpty() throws DataAccessException {
        var games = dataAccess.listOfGames();
        assertTrue(games.isEmpty());
    }

    @Test
    void updateGameSuccess() throws DataAccessException {
        GameData game = new GameData(1, null, null, "testGame", new ChessGame());
        dataAccess.createGame(game);
        GameData game2 = new GameData(1, "white", null, "updateGame", new ChessGame());
        dataAccess.updateGame(game2);
        GameData game3 = dataAccess.getGame(1);
        assertEquals("white", game3.whiteUsername());
        assertEquals("updateGame", game3.gameName());
    }

    @Test
    void updateNonexistentGameFailure() throws DataAccessException {
        GameData game = new GameData(999, null, null, "testGame", new ChessGame());
        assertThrows(DataAccessException.class, () -> dataAccess.updateGame(game));
    }

    @Test
    void createAuthorizationSuccess() throws DataAccessException {
        AuthData authData = new AuthData("token", "user");
        dataAccess.createAuthorization(authData);
        AuthData authData2 = dataAccess.getAuthorization("token");
        assertNotNull(authData2);
        assertEquals("user", authData2.username());
    }

    @Test
    void createDuplicateAuthorizationFailure() throws DataAccessException {
        AuthData authData = new AuthData("token", "user");
        dataAccess.createAuthorization(authData);
        assertThrows(DataAccessException.class, () -> dataAccess.createAuthorization(authData));
    }

    @Test
    void getAuthorizationSuccess() throws DataAccessException {
    }

    @Test
    void getNonExistentAuthorizationFailure() throws DataAccessException {
    }

    @Test
    void deleteAuthorizationSuccess() throws DataAccessException {
    }

    @Test
    void deleteNonexistentAuthorizationFailure() throws DataAccessException {
    }
}