package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
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

    private void setStringToNull(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
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
        String sql = "SELECT username, password, email FROM users WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery(sql)) {
                if(rs.next()){
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                            );
                }
            }
        }catch(SQLException e){
            throw new DataAccessException("Unable to create user: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (gameID, whiteUsername, blackUsername, gameName, gameState) VALUES (?, ?, ?, ?, ?)";
        String gameJson = gson.toJson(game.game());
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)){
            ps.setInt(1, game.gameID());
            setStringToNull(ps, 2, game.whiteUsername());
            setStringToNull(ps, 3, game.blackUsername());
            ps.setString(4, game.gameName());
            ps.setString(5, gameJson);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create game: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT * FROM games WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)){
            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                if(rs.next()){
                    ChessGame game = gson.fromJson(rs.getString("gameState"), ChessGame.class);
                    return new GameData(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            game
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get game: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<GameData> listOfGames() throws DataAccessException {
        List<GameData> gameList = new ArrayList<>();
        String sql = "SELECT * FROM games";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                ChessGame game = gson.fromJson(rs.getString("gameState"), ChessGame.class);
                gameList.add(new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        game
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to list games: " + e.getMessage());
        }
        return gameList;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, gameState = ? WHERE gameID = ?";
        String gameJson = gson.toJson(game.game());
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)){
            setStringToNull(ps, 1, game.whiteUsername());
            setStringToNull(ps, 2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, gameJson);
            ps.setInt(5, game.gameID());
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DataAccessException("Game not found");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update game: " + e.getMessage());
        }
    }

    @Override
    public void createAuthorization(AuthData auth) throws DataAccessException {

    8}

    @Override
    public AuthData getAuthorization(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuthorization(String authToken) throws DataAccessException {

    }
}
