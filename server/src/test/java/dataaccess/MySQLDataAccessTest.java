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
        try {
            dataAccess.configureDatabase();
        } catch (DataAccessException e) {

        }
        dataAccess.clear();
    }

    @Test
    void clearSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("test", "password", "test@email.com"));
        dataAccess.clear();
        assertNull(dataAccess.getUser("test"));
    }

    @Test
    void clearFailure() throws DataAccessException {
        assertDoesNotThrow(() -> dataAccess.clear());
    }



    @Test
    void createGameIDSuccess() throws DataAccessException {
        String gameName = "test";
        int id = dataAccess.createGameID(gameName);
        assertTrue(id > 0);
    }

    @Test
    void createGameIDFailure() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> dataAccess.createGameID(null));
    }



    @Test
    void createUserSuccess() throws DataAccessException {
        UserData user = new UserData("test", "password", "test@email.com");
        dataAccess.createUser(user);
        UserData user2 = dataAccess.getUser("test");
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
        UserData user2 = dataAccess.getUser("test");
        assertNotNull(user2);
        assertEquals("test", user2.username());
    }

    @Test
    void getNonexistentUserFailure() throws DataAccessException {
        assertNull(dataAccess.getUser("nonexistent"));
    }

    @Test
    void getGameSuccess() throws DataAccessException {
        int gameID = dataAccess.createGameID("test");
        GameData game = dataAccess.getGame(gameID);
        assertNotNull(game);
        assertEquals("test", game.gameName());
    }

    @Test
    void getNonExistentGameFailure() throws DataAccessException {
        assertNull(dataAccess.getGame(999));
    }

    @Test
    void listOfGamesSuccess() throws DataAccessException {
        dataAccess.createGameID("test");
        dataAccess.createGameID("test2");
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
        dataAccess.createUser(new UserData("whitePlayer", "password", "white@email.com"));
        dataAccess.createUser(new UserData("blackPlayer", "password", "black@email.com"));
        int gameID = dataAccess.createGameID("test");
        GameData updatedGame = new GameData(gameID, "whitePlayer", "blackPlayer", "updatedGame", new ChessGame());
        dataAccess.updateGame(updatedGame);
        GameData retrieve = dataAccess.getGame(gameID);
        assertEquals("updatedGame", retrieve.gameName());
        assertEquals("whitePlayer", retrieve.whiteUsername());
    }

    @Test
    void updateNonexistentGameFailure() throws DataAccessException {
        GameData game = new GameData(999, null, null, "testGame", new ChessGame());
        assertThrows(DataAccessException.class, () -> dataAccess.updateGame(game));
    }

    @Test
    void createAuthorizationSuccess() throws DataAccessException {
        UserData user = new UserData("user", "password", "test@email.com");
        dataAccess.createUser(user);
        AuthData authData = new AuthData("token", "user");
        dataAccess.createAuthorization(authData);
        AuthData authData2 = dataAccess.getAuthorization("token");
        assertNotNull(authData2);
        assertEquals("user", authData2.username());
    }

    @Test
    void createDuplicateAuthorizationFailure() throws DataAccessException {
        UserData user = new UserData("user", "password", "test@email.com");
        dataAccess.createUser(user);
        AuthData authData = new AuthData("token", "user");
        dataAccess.createAuthorization(authData);
        assertThrows(DataAccessException.class, () -> dataAccess.createAuthorization(authData));
    }

    @Test
    void getAuthorizationSuccess() throws DataAccessException {
        UserData user = new UserData("user", "password", "test@email.com");
        dataAccess.createUser(user);

        AuthData authData = new AuthData("token", "user");
        dataAccess.createAuthorization(authData);
        AuthData authData2 = dataAccess.getAuthorization("token");
        assertNotNull(authData2);
        assertEquals("user", authData2.username());
    }

    @Test
    void getNonExistentAuthorizationFailure() throws DataAccessException {
        assertNull(dataAccess.getAuthorization("nonexistent"));
    }

    @Test
    void deleteAuthorizationSuccess() throws DataAccessException {
        UserData user = new UserData("test", "password", "test@email.com");
        dataAccess.createUser(user);
        AuthData authData = new AuthData("token", "test");
        dataAccess.createAuthorization(authData);
        dataAccess.deleteAuthorization("token");
        assertNull(dataAccess.getAuthorization("token"));
    }

    @Test
    void deleteNonexistentAuthorizationFailure() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> dataAccess.deleteAuthorization("nonexistent"));
    }

    @Test
    void configureDatabaseSuccess() throws DataAccessException {
        MySQLDataAccess dataAccess = new MySQLDataAccess();
        assertDoesNotThrow(dataAccess::configureDatabase);
        dataAccess.createUser(new UserData("user", "password", "test@email.com"));
        assertNotNull(dataAccess.getUser("user"));
    }

    @Test
    void configureDatabaseFailure() throws DataAccessException {
        assertDoesNotThrow(() -> dataAccess.configureDatabase());
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("whitePlayer", "passwordW", "white@email.com"));
        dataAccess.createUser(new UserData("blackPlayer", "passwordB", "black@email.com"));
        GameData game = new GameData(0, "whitePlayer", "blackPlayer", "updatedGame", new ChessGame());
        int gameID = dataAccess.createGame(game);
        GameData retrieve = dataAccess.getGame(gameID);
        assertNotNull(retrieve);
        assertEquals("updatedGame", retrieve.gameName());
    }

    @Test
    void createGameDuplicateFailure() throws DataAccessException {
        dataAccess.createUser(new UserData("whitePlayer", "password1", "white@email.com"));
        GameData game = new GameData(0, "whitePlayer", "blackPlayerDoesNotExist", "testGame", new ChessGame());
        assertThrows(DataAccessException.class, () -> dataAccess.createGame(game));
    }
}