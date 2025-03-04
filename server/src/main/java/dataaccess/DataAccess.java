package dataaccess;

import model.*;


public interface DataAccess {
    void clear() throws DataAccessException;
    int createGameID() throws DataAccessException;
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    java.util.List<GameData> listOfGames() throws  DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    void createAuthorization(AuthData auth) throws DataAccessException;
    AuthData getAuthorization(String authToken) throws DataAccessException;
    void deleteAuthorization(String authToken) throws DataAccessException;

}
