package dataaccess;

import chess.*;
import com.google.gson.*;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This java file handles all data access operations while using a MySQL database.
 */
public class MySQLDataAccess implements DataAccess {
    private final String[] sqlStatements = {
            "CREATE TABLE IF NOT EXISTS users (" +
                "username VARCHAR (255) NOT NULL PRIMARY KEY, " +
                "password VARCHAR (255) NOT NULL, " +
                "email VARCHAR (255) NOT NULL)",
            "CREATE TABLE IF NOT EXISTS games (" +
                "gameID INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "whiteUsername VARCHAR (255)," +
                "blackUsername VARCHAR (255)," +
                "gameName VARCHAR (255) NOT NULL," +
                "game TEXT NOT NULL," +
                "FOREIGN KEY (whiteUsername) REFERENCES users (username), " +
                "FOREIGN KEY (blackUsername) REFERENCES users (username))",
            "CREATE TABLE IF NOT EXISTS authorization_data (" +
                "authToken VARCHAR (255) NOT NULL PRIMARY KEY, " +
                "username VARCHAR (255) NOT NULL, " +
                "FOREIGN KEY (username) REFERENCES users (username))"
    };

    private final Gson gson;

    public MySQLDataAccess() {
        gson = configureGson();
    }

    private Gson configureGson() {
        return new GsonBuilder()
                .registerTypeAdapter(ChessGame.class, new AdaptChessGame())
                .registerTypeAdapter(ChessBoard.class, new AdaptChessBoard())
                .registerTypeAdapter(ChessPosition.class, new AdaptChessPosition())
                .create();
    }
    private static class AdaptChessGame implements JsonSerializer<ChessGame>, JsonDeserializer<ChessGame> {
        @Override
        public JsonElement serialize(ChessGame chessGame, Type type,
            JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("board",
                    jsonSerializationContext.serialize(chessGame.getBoard()));
            jsonObject.addProperty("teamTurn", chessGame.getTeamTurn().toString());
            jsonObject.addProperty("gameOver", chessGame.isGameOver());
            return jsonObject;
        }

        @Override
        public ChessGame deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            ChessGame game = new ChessGame();
            game.setBoard(jsonDeserializationContext.deserialize(jsonObject.get("board"), ChessBoard.class));
            game.setTeamTurn(ChessGame.TeamColor.valueOf(jsonObject.get("teamTurn").getAsString()));
            if (jsonObject.has("gameOver")) {
                game.setGameOver(jsonObject.get("gameOver").getAsBoolean());
            }
            return game;
        }
    }

    private static class AdaptChessBoard implements JsonSerializer<ChessBoard>, JsonDeserializer<ChessBoard> {

        @Override
        public JsonElement serialize(ChessBoard chessBoard, Type type,
            JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            for(Map.Entry<ChessPosition, ChessPiece> entry : chessBoard.getBoard().entrySet()) {
                String key = String.valueOf(entry.getKey().getRow()) + ","
                        + String.valueOf(entry.getKey().getColumn());
                jsonObject.add(key, jsonSerializationContext.serialize(entry.getValue()));
            }
            return jsonObject;
        }

        @Override
        public ChessBoard deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            ChessBoard board = new ChessBoard();
            for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String[] place = entry.getKey().split(",");
                int row = Integer.parseInt(place[0]);
                int column = Integer.parseInt(place[1]);
                ChessPiece piece = jsonDeserializationContext.deserialize(entry.getValue(), ChessPiece.class);
                board.addPiece(new ChessPosition(row, column), piece);
            }
            return board;
        }

    }

    private static class AdaptChessPosition implements JsonSerializer<ChessPosition>, JsonDeserializer<ChessPosition> {
        @Override
        public ChessPosition deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            int row = jsonObject.get("row").getAsInt();
            int column = jsonObject.get("column").getAsInt();
            return new ChessPosition(row, column);
        }

        @Override
        public JsonElement serialize(ChessPosition chessPosition,
            Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("row", chessPosition.getRow());
            jsonObject.addProperty("column", chessPosition.getColumn());
            return jsonObject;
        }
    }


    public void configureDatabase()  throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            String[] tables = {"users", "games", "authorization_data"};
            for (int i = 0; i < tables.length; i++) {
                if (!checkTable(conn, tables[i])) {
                    try (var ps = conn.prepareStatement(sqlStatements[i])) {
                        ps.executeUpdate();
                    }
                }
            }
            try (var stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT 1 FROM users LIMIT 1;");
            } catch (SQLException e) {
                for (int i = 0; i < tables.length; i++) {
                    try (var ps = conn.prepareStatement("DROP TABLE IF EXISTS" + tables[i])) {
                        ps.executeUpdate();
                    }
                    try (var ps = conn.prepareStatement(sqlStatements[i])) {
                        ps.executeUpdate();
                    }
                }
            }
        }catch(SQLException e){
            throw new DataAccessException("Unable to configure database: " + e.getMessage());
        }
    }

    private boolean checkTable(Connection conn, String table) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, table, new String[]{"TABLE"})) {
            return rs.next();
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
        String[] tables = {"games", "authorization_data", "users"};
        try (Connection conn = DatabaseManager.getConnection()){
            for (String table : tables) {
                try (var ps = conn.prepareStatement("DELETE FROM " + table)){
                    ps.executeUpdate();
                }
            }
        }catch(SQLException e){
            throw new DataAccessException("Unable to clear database: " + e.getMessage());
        }
    }

    @Override
    public int createGameID(String gameName) throws DataAccessException {
        String sql = "INSERT INTO games (gameName, game) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, gameName);
            ps.setString(2, gson.toJson(new ChessGame()));
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
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.username());
            ps.setString(2, user.password());
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
            try (var rs = ps.executeQuery()) {
                if(rs.next()){
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                }
            }
        }catch(SQLException e){
            throw new DataAccessException("Unable to get requested user: " + e.getMessage());
        }
        return null;
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        String gameJson = gson.toJson(game.game());

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            setStringToNull(ps, 1, game.whiteUsername());
            setStringToNull(ps, 2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, gameJson);
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()){
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new DataAccessException("Unable to generate game ID: ");
            }
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
                    ChessGame game = gson.fromJson(rs.getString("game"), ChessGame.class);
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
                ChessGame game = gson.fromJson(rs.getString("game"), ChessGame.class);
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
        String sql = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";
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
        String sql = "INSERT INTO authorization_data (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)){
            ps.setString(1, auth.authToken());
            ps.setString(2, auth.username());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create authorization: " + e.getMessage());
        }
    }

    @Override
    public AuthData getAuthorization(String authToken) throws DataAccessException {
        String sql = "SELECT authToken, USERNAME FROM authorization_data WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)){
            ps.setString(1, authToken);
            try (var rs = ps.executeQuery()) {
                if(rs.next()){
                    return new AuthData(
                            rs.getString("authToken"),
                            rs.getString("username")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get authorization: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteAuthorization(String authToken) throws DataAccessException {
        String sql = "DELETE FROM authorization_data WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DataAccessException("Authorization token not found");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to delete authorization: " + e.getMessage());
        }
    }
}