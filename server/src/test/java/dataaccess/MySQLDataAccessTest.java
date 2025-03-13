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
    void createGameIDSuccess() throws DataAccessException {
        String gameName = "test";
        int id = dataAccess.createGameID(gameName);
        assertTrue(id > 0);
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
    void getNonExistentGameFailure() throws DataAccessException {
        assertNull(dataAccess.getGame(999));
    }


    @Test
    void listOfGamesIsEmpty() throws DataAccessException {
        var games = dataAccess.listOfGames();
        assertTrue(games.isEmpty());
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
}