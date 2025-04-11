package server;

import chess.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import model.AuthData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;
import service.UserService;
import websocket.commands.*;
import websocket.messages.*;

import model.GameData;
import java.io.IOException;
import java.util.HashMap;
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
        // Log connection (optional)
        System.out.println("WebSocket connected: " + session.getRemoteAddress().getAddress());
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        for (Map<Session, Connection> connections : gameConnections.values()) {
            if (connections.containsKey(session)) {
                connections.remove(session);
            }
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            String authToken = command.getAuthToken();
            Integer gameID = command.getGameID();

            AuthData authData;
            try {
                authData = userService.verifyAuthToken(authToken);
            } catch (DataAccessException e) {
                sendError(session, "Invalid auth token");
                return;
            }

            switch (command.getCommandType()) {
                case CONNECT:
                    handleConnect(session, authData.username(), gameID);
                    break;
                case MAKE_MOVE:
                    break;
                case LEAVE:
                    break;
                case RESIGN:
                    break;
                default:
                    sendError(session, "Unknown command type");
            }
        } catch (Exception e) {
            sendError(session, "Error processing message: " + e.getMessage());
        }
    }

    private void handleConnect(Session session, String username, Integer gameID) throws IOException {
        try {
            GameData gameData = gameService.getGame(gameID);
            if (gameData == null) {
                sendError(session, "Game not found");
                return;
            }
            boolean isPlayer = username.equals(gameData.whiteUsername()) || username.equals(gameData.blackUsername());

            // Store the connection
            Map<Session, Connection> connections = gameConnections.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>());
            connections.put(session, new Connection(session, username, isPlayer));

            // Send LOAD_GAME message to the connecting user with the ChessGame
            ServerMessage loadGameMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());
            session.getRemote().sendString(gson.toJson(loadGameMessage));

            // Notify other users in the game
            String notificationText = String.format("%s %s the game.", username, isPlayer ? "joined" : "is observing");
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationText);

            for (Map.Entry<Session, Connection> entry : connections.entrySet()) {
                Session otherSession = entry.getKey();
                if (!otherSession.equals(session) && otherSession.isOpen()) {
                    otherSession.getRemote().sendString(gson.toJson(notification));
                }
            }
        } catch (DataAccessException e) {
            sendError(session, "Error accessing game data: " + e.getMessage());
        }
    }

    private void sendError(Session session, String errorMessage) throws IOException {
        ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
        if (session.isOpen()) {
            session.getRemote().sendString(gson.toJson(error));
        }
    }
}