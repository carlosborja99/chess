package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AccessDataInMemory extends DataAccess {
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, GameData> games = new HashMap<>();
    private final Map<String, AuthData> authTokens = new HashMap<>();

    @Override
    public void clear(){
        users.clear();
        games.clear();
        authTokens.clear();
    }
    @Override
    public void createUser(UserData user) throws DataAccessException{
        users.put(user.username(), user);
    }
    @Override
    public UserData getUser(String username) throws DataAccessException{
        return users.get(username);
    }

    @Override
    public void createGame(GameData newGame) throws DataAccessException{
        games.put(String.valueOf(newGame.gameID()), newGame);
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
        games.put(String.valueOf(game.gameID()), game);
    }
    @Override
    public void createAuthorization(AuthData a) throws DataAccessException{
        authTokens.put(a.authToken(), a);
    }
    @Override
    public AuthData getAuthorization(String authToken) throws DataAccessException{
        return authTokens.get(authToken);
    }
    @Override
    public void deleteAuthorization(String authToken) throws DataAccessException{
        authTokens.remove(authToken);
    }
}
