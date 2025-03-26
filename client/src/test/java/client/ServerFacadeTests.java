package client;

import org.junit.jupiter.api.*;
import server.Server;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void registerSuccess() throws Exception {
        Map<String, Object> authData = facade.register("player", "password", "player@email.com");
        assertNotNull(authData.get("authToken"));
        assertTrue(((String) authData.get("authToken")).length() > 10);
        assertEquals("player", authData.get("username"));
    }



}
