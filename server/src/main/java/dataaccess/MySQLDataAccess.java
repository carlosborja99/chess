package dataaccess;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;

public class MySQLDataAccess implements DataAccess {
    private final Gson gson = new Gson();

    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public int createGameID() throws DataAccessException {
        return 0;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {

    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public List<GameData> listOfGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }

    @Override
    public void createAuthorization(AuthData auth) throws DataAccessException {

    }

    @Override
    public AuthData getAuthorization(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuthorization(String authToken) throws DataAccessException {

    }
}
