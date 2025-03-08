package dataaccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MySQLDataAccessTest {
    private MySQLDataAccess dataAccess;
    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MySQLDataAccess();
        dataAccess.clear();
    }

    @Test
    void clearSuccess() throws DataAccessException {
    }

    @Test
    void createGameIDSuccess() throws DataAccessException {
    }

    @Test
    void createUserSuccess() throws DataAccessException {
    }

    @Test
    void createDuplicateUserFailure() throws DataAccessException {
    }

    @Test
    void getUserSuccess() throws DataAccessException {
    }

    @Test
    void getNonexistentUserFailure() throws DataAccessException {
    }

    @Test
    void createGameSuccess() throws DataAccessException {
    }

    @Test
    void createDuplicateGameFailure() throws DataAccessException {
    }

    @Test
    void getGameSuccess() throws DataAccessException {
    }

    @Test
    void getNonExistentGameFailure() throws DataAccessException {
    }

    @Test
    void listOfGamesSuccess() throws DataAccessException {
    }

    @Test
    void listOfGamesIsEmpty() throws DataAccessException {
    }

    @Test
    void updateGameSuccess() throws DataAccessException {
    }

    @Test
    void updateNonexistentGameFailure() throws DataAccessException {
    }

    @Test
    void createAuthorizationSuccess() throws DataAccessException {
    }

    @Test
    void createDuplicateAuthorizationFailure() throws DataAccessException {
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