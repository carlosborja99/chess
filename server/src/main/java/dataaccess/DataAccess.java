package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;

public abstract class DataAccess {
    public abstract void clear() throws DataAccessException;
    public abstract void createUser(UserData user) throws DataAccessException;
    public abstract UserData getUser(String username) throws DataAccessException;
    public abstract void createGame(GameData game) throws DataAccessException;
    public abstract GameData getGame(int gameID) throws DataAccessException;
    public abstract ArrayList<GameData> listOfGames() throws  DataAccessException;
    public abstract void updateGame(GameData game) throws DataAccessException;
    public abstract void createAuthorization(AuthData a) throws DataAccessException;
    public abstract AuthData getAuthorization(String authToken) throws DataAccessException;
    public abstract void deleteAuthorization(String authToken) throws DataAccessException;
}
