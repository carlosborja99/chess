package dataaccess;

import model.*;
import java.util.*;

public class AccessDataInMemory implements DataAccess {
    private Map<String, UserData> users = new HashMap<>();
    private Map<Integer, GameData> games = new HashMap<>();
    private Map<String, AuthData> authTokens = new HashMap<>();
    private int nextGameId = 1;

    @Override
    public void clear()throws DataAccessException{
        users.clear();
        games.clear();
        authTokens.clear();
        nextGameId = 1;
    }

    @Override
    public int createGameID() throws DataAccessException {
        return nextGameId++;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())){
            throw new DataAccessException("User already exists");
        }
        users.put(user.username(), user);
    }
    @Override
    public UserData getUser(String username) throws DataAccessException{
        return users.get(username);
    }

    @Override
    public void createGame(GameData newGame) throws DataAccessException{
        games.put(newGame.gameID(), newGame);
        nextGameId++;
    }
    @Override
    public GameData getGame(int gameID) throws DataAccessException{
        return games.get(gameID);
    }
    @Override
    public ArrayList<GameData> listOfGames() throws DataAccessException{
        return new ArrayList<>(games.values());
    }
    @Override
    public void updateGame(GameData game)  throws DataAccessException{
        if (!games.containsKey(game.gameID())){
            throw new DataAccessException("Game not found");
        }
        games.put(game.gameID(), game);
    }
    @Override
    public void createAuthorization(AuthData auth) throws DataAccessException{
        authTokens.put(auth.authToken(), auth);
    }
    @Override
    public AuthData getAuthorization(String authToken) throws DataAccessException{
        return authTokens.get(authToken);
    }
    @Override
    public void deleteAuthorization(String authToken) throws DataAccessException{
        if (!authTokens.containsKey(authToken)){
            throw new DataAccessException("Authorization token not found");
        }
        authTokens.remove(authToken);
    }
}
