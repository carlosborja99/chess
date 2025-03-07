package dataaccess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.List;

public class MySQLDataAccess implements DataAccess {
    private final Gson gson = new Gson();

    private final String[] SQLStatement = {
            """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR (255) NOT NULL PRIMARY KEY,
                password VARCHAR (255) NOT NULL,
                email VARCHAR (255) NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS games (
                gameID INT NOT NULL PRIMARY KEY,
                whiteUsername VARCHAR (255),
                blackUsername VARCHAR (255),
                gameName VARCHAR (255) NOT NULL,
                game TEXT NOT NULL,
                FOREIGN KEY (whiteUsername) REFERENCES user (username),
                FOREIGN KEY (blackUsername) REFERENCES user (username)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS authorization_data (
                authToken VARCHAR (255) NOT NULL PRIMARY KEY,
                username VARCHAR (255) NOT NULL,
                FOREIGN KEY (username) REFERENCES user (username)
            )
            """
    };


    public MySQLDataAccess() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase()  throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (String statement : SQLStatement) {
                try (var ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }
        }catch(SQLException e){
            throw new DataAccessException("Unable to configure database: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String[] tables = { "games", "users", "authorization_data" };
        try (var conn = DatabaseManager.getConnection()){
            for (String table : tables) {
                try (var ps = conn.prepareStatement("TRUNCATE TABLE " + table)){
                    ps.executeUpdate();
                }
            }
        }catch(SQLException e){
            throw new DataAccessException("Unable to clear database: " + e.getMessage());
        }
    }

    @Override
    public int createGameID() throws DataAccessException {
        String sql = "INSERT INTO games (gameName) VALUES (?)";
        try (var conn = DatabaseManager.getConnection();
            var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, "New Game");
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()){
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new DataAccessException("Unable to retrieve game ID");
            }
        }catch(SQLException e){
            throw new DataAccessException("Unable to create game ID: " + e.getMessage());
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        try (var conn = DatabaseManager.getConnection();
            var ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.username());
            ps.setString(2, hashedPassword);
            ps.setString(3, user.email());
            ps.executeUpdate();
        } catch(SQLException e){
            throw new DataAccessException("Unable to create user: " + e.getMessage());
        }
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
