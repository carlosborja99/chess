package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;

/**
 * Defines the data access operations for the Chess application.
 * All the methods may throw DataAccessException if operations fail.
 */
public interface DataAccess {
    /**
     *Removes all the data from the storage system.
     */
    void clear() throws DataAccessException;

    /**
     * Creates a unique game ID for a given game name.
     * @param gameName is the name of the CHess game.
     * @return the generated game ID.
     */
    int createGameID(String gameName) throws DataAccessException;

    /**
     * Creates a new user in the storage system.
     * @param user is the user data to store.
     */
    void createUser(UserData user) throws DataAccessException;

    /**
     * Retrieves a user's data based on the user's username.
     * @param username is for the username to look up.
     * @return the userdata if it is found, null if it is not found (generally it means that the userdata does not exist).
     */
    UserData getUser(String username) throws DataAccessException;

    /**
     * Create a new game in the storage system.
     * @param game is the game data to store the information.
     * @return the game ID of the created game.
     */
    int createGame(GameData game) throws DataAccessException;

    /**
     * Retrieves a game by its ID.
     * @param gameID is the ID fof the game for the program to retrieve.
     * @return the GameData if its found, but null otherwise.
     */
    GameData getGame(int gameID) throws DataAccessException;

    /**
     * List of all games in the storage system.
     * @return a List that contains all GameData objects.
     */
    List<GameData> listOfGames() throws  DataAccessException;

    /**
     * Updates game with new information
     * @param game is the updated game data.
     */
    void updateGame(GameData game) throws DataAccessException;

    /**
     * Makes a new authorization token.
     * @param auth is the authorization data to store
     */
    void createAuthorization(AuthData auth) throws DataAccessException;

    /**
     * Retrieves authorization data based on an Authorization Token.
     * @param authToken is the Authorization token to look up
     * @return the Authorization data (AuthData) if found, if not, return null.
     */
    AuthData getAuthorization(String authToken) throws DataAccessException;

    /**
     *
     * Deletes authorization token from the storage system/database.
     * @param authToken is the TOken to delete.
     */
    void deleteAuthorization(String authToken) throws DataAccessException;
}
