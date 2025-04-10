package server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    private final UserService userService;
    private final GameService gameService;
    private final Gson gson = new Gson();
    private final Map<Integer, Map<Session, Connection>> gameConnections = new ConcurrentHashMap<>();

    private static class Connection {
        Session session;
        String username;
        boolean isPlayer;
        Connection(Session session, String username, boolean isPlayer) {
            this.session = session;
            this.username = username;
            this.isPlayer = isPlayer;
        }
    }
    public WebSocketHandler(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }
    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {

    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        for (Map<Session, Connection> connections : gameConnections.values()) {
            if (connections.containsKey(session)) {
                connections.remove(session);
            }
        }
    }

}
