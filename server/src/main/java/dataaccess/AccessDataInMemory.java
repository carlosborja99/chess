package dataaccess;

import model.SharedModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AccessDataInMemory {
    private Map<String, SharedModule.UserData> users = new HashMap<>();
    private Map<String, SharedModule.GameData> games = new HashMap<>();
    private Map<String, SharedModule.AuthData> authTokens = new HashMap<>();

    @Override
    public void clear(){
        users.clear();
        games.clear();
        authTokens.clear();
    }
    @Override
    public void createUser(SharedModule.UserData user){
        users.put(user.username(), user);
    }
    @Override
    public SharedModule.UserData getUser(String username){
        return users.get(username);
    }

    @Override
    public void createGame(SharedModule.GameData newGame){
        games.put(String.valueOf(newGame.gameID()), newGame);
    }
    @Override
    public SharedModule.GameData getGame(int gameID){
        return games.get(gameID);
    }
    @Override
    public ArrayList<SharedModule.GameData> listOfGames(){
        return new ArrayList<>(games.values());
    }
    @Override
    public void updateGame(SharedModule.GameData game){
        games.put(String.valueOf(game.gameID()), game);
    }
    @Override
    public void createAuthorization(SharedModule.AuthData a){
        authTokens.put(a.authToken(), a);
    }
    @Override
    public SharedModule.AuthData getAuthorization(String authToken){
        return authTokens.get(authToken);
    }
    @Override
    void deleteAuthorization(String authToken){
        authTokens.remove(authToken);
    }
}
