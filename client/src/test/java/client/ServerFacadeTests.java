package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;
import server.Server;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static int port; // Stores the port number

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDB() throws Exception {
        HttpURLConnection connection = newConnection("/db", "DELETE", null);
        Map<String, Object> result = getResponse(connection);
        assertTrue(result.containsKey("success") && (Boolean) result.get("success"), "Database clear failed.");
    }

    private HttpURLConnection newConnection(String point, String method, String authToken) throws Exception{
        URI uri = new URI(facade.serverURL + point);
        HttpURLConnection connect = (HttpURLConnection) uri.toURL().openConnection();
        connect.setRequestMethod(method);
        if (authToken != null) {
            connect.setRequestProperty("Authorization", authToken);
        }
        return connect;
    }

    private Map<String, Object> getResponse(HttpURLConnection connection) throws Exception {
        int status = connection.getResponseCode();
        if (status >= 200 && status < 300) {
            try (var in = connection.getInputStream()) {
                if (in.available() == 0) {
                    return Map.of();
                }
                return new Gson().fromJson(new InputStreamReader(in), new TypeToken<Map<String, Object>>(){}.getType());
            }
        } else {
            try (var in = connection.getErrorStream()) {
                String error = new Gson().fromJson(new InputStreamReader(in), Map.class).get("message").toString();
                throw new Exception(error);
            }
        }
    }

    @Test
    void registerSuccess() throws Exception {
        Map<String, Object> authData = facade.register("player", "password", "player@email.com");
        assertNotNull(authData.get("authToken"));
        assertTrue(((String) authData.get("authToken")).length() > 10);
        assertEquals("player", authData.get("username"));
    }

    @Test
    void registerDuplicateFailure() throws Exception {
        facade.register("player", "password", "player@email.com");
        Exception exception = assertThrows(Exception.class, () -> facade.register("player", "newPassword", "newemail@email.com"));
        assertTrue(exception.getMessage().contains("already"));
    }

    @Test
    void positiveLogin() throws Exception {
        facade.register("player", "password", "player@email.com");
        Map<String, Object> authData = facade.login("player", "password");
        assertNotNull(authData.get("authToken"));
        assertTrue(((String) authData.get("authToken")).length() > 10);
        assertEquals("player", authData.get("username"));
    }

    @Test
    void loginPasswordFail() throws Exception {
        facade.register("player", "password", "player@email.com");
        Exception exception = assertThrows(Exception.class, () -> facade.login("player", "wrongPassword"));
        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    void loginNonExistentUserFailure() throws Exception {
        Exception exception = assertThrows(Exception.class, () -> facade.login("thisPlayerDoesNotExist", "password"));
        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    void JoinGameSuccess() throws Exception{
        facade.register("player", "password", "player@email.com");
        Map<String, Object> gameData = facade.createMyGame("GameTest");
        String gameID = gameData.get("gameID").toString();
        assertDoesNotThrow(() -> facade.joinGame(gameID, "WHITE"));
        var games = facade.listOfGames();
        assertFalse(games.isEmpty(), "Game list should not be empty");
        assertEquals("player", games.get(0).get("whiteUsername"), "Player should be in the white team");
    }

    @Test
    void joinGameInvalidColorFailure() throws Exception {
        facade.register("player", "password", "player@email.com");
        Map<String, Object> gameData = facade.createMyGame("GameTest");
        String gameID = gameData.get("gameID").toString();
        Exception exception = assertThrows(Exception.class, () -> facade.joinGame(gameID, "INVALID ID"));
        assertTrue(exception.getMessage().contains("Bad Request"), "THe expected error contains 'Bad Request', but got: " + exception.getMessage());
    }

    @Test
    void joinGameAlreadyTakenFailure() throws Exception {
        facade.register("player", "password", "player@email.com");
        Map<String, Object> gameData = facade.createMyGame("GameTest");
        String gameID = gameData.get("gameID").toString();
        facade.joinGame(gameID, "BLACK");
        Exception exception = assertThrows(Exception.class, () -> facade.joinGame(gameID, "BLACK"));
        assertTrue(exception.getMessage().contains("taken"));
    }

    @Test
    void joinGameNotValidColorFailure() throws Exception {
        facade.register("player", "password", "player@email.com");
        Map<String, Object> gameData = facade.createMyGame("GameTest");
        String gameID = gameData.get("gameID").toString();
        Exception exception = assertThrows(Exception.class, () -> facade.joinGame(gameID, "INVALID"));
        assertTrue(exception.getMessage().contains("Bad Request"));
    }

    @Test
    void successfullyCreateGame() throws Exception {
        facade.register("player", "password", "player@email.com");
        Map<String, Object> gameData = facade.createMyGame("GameTest");
        assertNotNull(gameData.get("gameID"));
        assertTrue(((Double) gameData.get("gameID")) > 0);
    }

    @Test
    void unauthorizedCreateGameFailure() throws Exception {
        Exception exception = assertThrows(Exception.class, () -> facade.createMyGame("GameTest"));
        assertTrue(exception.getMessage().contains("Unauthorized"));
    }

        @Test
    void ListMyGamesSuccess() throws Exception {
        facade.register("player", "password", "player@email.com");
        facade.createMyGame("GameTest");
        List <Map<String, Object>> games = facade.listOfGames();
        assertFalse(games.isEmpty());
        assertEquals("GameTest", games.getFirst().get("gameName"));
    }

    @Test
    void unauthorizedGamesListRequestFailure() throws Exception {
        Exception exception = assertThrows(Exception.class, () -> facade.listOfGames());
        assertTrue(exception.getMessage().contains("Unauthorized"));
    }

}
