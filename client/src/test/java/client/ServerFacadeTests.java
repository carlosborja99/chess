package client;

import org.junit.jupiter.api.*;
import server.Server;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void cleanUp() throws Exception {
        facade.postRequest("/db", null, null);
    }

    @Test
    void registerSuccess() throws Exception {
        Map<String, Object> authData = facade.register("player1", "password", "player1@email.com");
        assertNotNull(authData.get("authToken"));
        assertTrue(((String) authData.get("authToken")).length() > 10);
        assertEquals("player1", authData.get("username"));
    }


}
