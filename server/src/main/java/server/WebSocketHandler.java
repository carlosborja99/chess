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
                    handleMakeMove(session, authData.username(), gameID, message);
                    break;
                case LEAVE:
                    handleLeave(session, authData.username(), gameID);
                    break;
                case RESIGN:
                    handleResign(session, authData.username(), gameID);
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
            Map<Session, Connection> connections = gameConnections.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>());
            connections.put(session, new Connection(session, username, isPlayer));
            ServerMessage loadGameMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());
            session.getRemote().sendString(gson.toJson(loadGameMessage));
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

    private void handleMakeMove(Session session, String username, Integer gameID, String message) throws IOException {
        try {
            GameData gameData = gameService.getGame(gameID);
            if (gameData == null) {
                sendError(session, "Game not found");
                return;
            }
            if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
                sendError(session, "Observers cannot make moves");
                return;
            }
            if (gameData.game().isGameOver()) {
                sendError(session, "Game is over");
                return;
            }

            // Parse the move from the message
            JsonObject json = gson.fromJson(message, JsonObject.class);
            JsonObject moveJson = json.getAsJsonObject("move");
            JsonObject startJson = moveJson.getAsJsonObject("start");
            JsonObject endJson = moveJson.getAsJsonObject("end");
            ChessPosition startPos = new ChessPosition(
                    startJson.get("row").getAsInt(),
                    startJson.get("col").getAsInt()
            );
            ChessPosition endPos = new ChessPosition(
                    endJson.get("row").getAsInt(),
                    endJson.get("col").getAsInt()
            );
            ChessMove move = new ChessMove(startPos, endPos, null); // Assuming no promotion for simplicity

            // Validate and make the move
            ChessGame game = gameData.game();
            ChessGame.TeamColor playerColor = username.equals(gameData.whiteUsername()) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
            if (game.getTeamTurn() != playerColor) {
                sendError(session, "Not your turn");
                return;
            }
            try {
                game.makeMove(move);
            } catch (InvalidMoveException e) {
                sendError(session, "Invalid move: " + e.getMessage());
                return;
            }

            // Update game in database
            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            );
            gameService.updateGame(updatedGame);

            // Broadcast updated game to all connected clients
            Map<Session, Connection> connections = gameConnections.get(gameID);
            if (connections != null) {
                ServerMessage loadGameMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
                String notificationText = String.format("%s moved from %s to %s",
                        username,
                        positionToNotation(startPos),
                        positionToNotation(endPos)
                );
                ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationText);
                for (Map.Entry<Session, Connection> entry : connections.entrySet()) {
                    Session clientSession = entry.getKey();
                    if (clientSession.isOpen()) {
                        clientSession.getRemote().sendString(gson.toJson(loadGameMessage));
                        if (!entry.getValue().username.equals(username)) {
                            clientSession.getRemote().sendString(gson.toJson(notification));
                        }
                    }
                }
            }
        } catch (DataAccessException e) {
            sendError(session, "Error accessing game data: " + e.getMessage());
        }
    }

    private void handleLeave(Session session, String username, Integer gameID) throws IOException {
        try {
            GameData gameData = gameService.getGame(gameID);
            if (gameData == null) {
                sendError(session, "Game not found");
                return;
            }
            Map<Session, Connection> connections = gameConnections.get(gameID);
            if (connections != null) {
                connections.remove(session);
                String notificationText = String.format("%s left the game.", username);
                ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationText);
                for (Map.Entry<Session, Connection> entry : connections.entrySet()) {
                    Session otherSession = entry.getKey();
                    if (otherSession.isOpen()) {
                        otherSession.getRemote().sendString(gson.toJson(notification));
                    }
                }
            }
        } catch (DataAccessException e) {
            sendError(session, "Error accessing game data: " + e.getMessage());
        }
    }

    private void handleResign(Session session, String username, Integer gameID) throws IOException {
        try {
            GameData gameData = gameService.getGame(gameID);
            if (gameData == null) {
                sendError(session, "Game not found");
                return;
            }
            if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
                sendError(session, "Observers cannot resign");
                return;
            }
            if (gameData.game().isGameOver()) {
                sendError(session, "Game is already over");
                return;
            }

            // Mark game as over
            ChessGame game = gameData.game();
            game.setGameOver(true);
            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            );
            gameService.updateGame(updatedGame);

            // Notify all clients
            Map<Session, Connection> connections = gameConnections.get(gameID);
            if (connections != null) {
                String notificationText = String.format("%s resigned from the game.", username);
                ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationText);
                for (Map.Entry<Session, Connection> entry : connections.entrySet()) {
                    Session clientSession = entry.getKey();
                    if (clientSession.isOpen()) {
                        clientSession.getRemote().sendString(gson.toJson(notification));
                    }
                }
            }
        } catch (DataAccessException e) {
            sendError(session, "Error accessing game data: " + e.getMessage());
        }
    }

    private String positionToNotation(ChessPosition position) {
        char file = (char) ('a' + position.getColumn() - 1);
        int rank = position.getRow();
        return "" + file + rank;
    }

    private void sendError(Session session, String errorMessage) throws IOException {
        ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage, true);
        if (session.isOpen()) {
            session.getRemote().sendString(gson.toJson(error));
        }
    }
}