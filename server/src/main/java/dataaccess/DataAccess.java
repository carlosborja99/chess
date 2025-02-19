package dataaccess;

import model.SharedModule;

import java.util.ArrayList;

public class DataAccess {
    void clear() throws DataAccessException;
    void createUser(SharedModule.UserData user) throws DataAccessException;
    SharedModule.UserData getUser(String username) throws DataAccessException;
    void createGame(SharedModule.GameData game) throws DataAccessException;
    SharedModule.GameData getGame(int gameID) throws DataAccessException;
    ArrayList<SharedModule.GameData> listOfGames() throws  DataAccessException;
    void updateGame(SharedModule.GameData game) throws DataAccessException;
    void createAuthorization(SharedModule.AuthData a) throws DataAccessException;
    SharedModule.AuthData getAuthorization(String authToken) throws DataAccessException;
    void deleteAuthorization(String authToken) throws DataAccessException;
}
