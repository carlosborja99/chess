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
            ChessGame game = gameData.game();
            if (game.isGameOver()) {
                sendError(session, "Game is over");
                return;
            }

            ChessMove move = createChessMove(session, message);
            if (move == null) {
                return; // Error already sent in createChessMove
            }

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

            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            );
            gameService.updateGame(updatedGame);

            broadcastMoveUpdate(session, gameID, game, username, move);
        } catch (DataAccessException e) {
            sendError(session, "Error accessing game data: " + e.getMessage());
        } catch (Exception e) {
            sendError(session, "Invalid move format: " + e.getMessage());
        }
    }

    private ChessMove createChessMove(Session session, String message) throws IOException {
        JsonObject json = gson.fromJson(message, JsonObject.class);
        if (!json.has("move")) {
            sendError(session, "Invalid move: Missing move data");
            return null;
        }
        JsonObject moveJson = json.getAsJsonObject("move");
        if (!moveJson.has("startPosition") || !moveJson.has("endPosition")) {
            sendError(session, "Invalid move: Missing startPosition or endPosition");
            return null;
        }
        JsonObject startJson = moveJson.getAsJsonObject("startPosition");
        JsonObject endJson = moveJson.getAsJsonObject("endPosition");
        if (!startJson.has("row") || !startJson.has("col") || !endJson.has("row") || !endJson.has("col")) {
            sendError(session, "Invalid move: Missing row or col in positions");
            return null;
        }
        ChessPosition startPos = new ChessPosition(
                startJson.get("row").getAsInt(),
                startJson.get("col").getAsInt()
        );
        ChessPosition endPos = new ChessPosition(
                endJson.get("row").getAsInt(),
                endJson.get("col").getAsInt()
        );
        ChessPiece.PieceType promotionPiece = null;
        if (moveJson.has("promotionPiece") && !moveJson.get("promotionPiece").isJsonNull()) {
            String promotion = moveJson.get("promotionPiece").getAsString();
            promotionPiece = ChessPiece.PieceType.valueOf(promotion.toUpperCase());
        }
        return new ChessMove(startPos, endPos, promotionPiece);
    }

    private void broadcastMoveUpdate(Session session, Integer gameID, ChessGame game, String username, ChessMove move) throws IOException {
        Map<Session, Connection> connections = gameConnections.get(gameID);
        if (connections == null) {
            return;
        }

        ServerMessage loadGameMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
        ServerMessage notification = createMoveNotification(game, username, move);

        for (Map.Entry<Session, Connection> entry : connections.entrySet()) {
            sendMessagesToClient(entry.getKey(), session, loadGameMessage, notification);
        }
    }

    private ServerMessage createMoveNotification(ChessGame game, String username, ChessMove move) {
        boolean isGameOver = game.isGameOver();
        if (!isGameOver) {
            String startPos = positionToNotation(move.getStartPosition());
            String endPos = positionToNotation(move.getEndPosition());
            String notificationText = String.format("%s moved from %s to %s", username, startPos, endPos);
            return new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationText);
        }

        ChessGame.TeamColor playerColor = game.getTeamTurn();
        String gameOverText;
        if (game.isInCheckmate(playerColor)) {
            String winner = playerColor == ChessGame.TeamColor.WHITE ? "Black" : "White";
            gameOverText = String.format("%s is in checkmate. %s wins!", playerColor, winner);
        } else {
            gameOverText = "Game ended in stalemate.";
        }
        return new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, gameOverText);
    }
    private void sendMessagesToClient(Session clientSession, Session senderSession, ServerMessage loadGameMessage, ServerMessage notification) throws IOException {
        if (!clientSession.isOpen()) {
            return;
        }
        clientSession.getRemote().sendString(gson.toJson(loadGameMessage));
        if (!clientSession.equals(senderSession)) {
            clientSession.getRemote().sendString(gson.toJson(notification));
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
                Connection conn = connections.remove(session);
                if (conn != null && conn.isPlayer) {
                    GameData updatedGame = new GameData(
                            gameData.gameID(),
                            username.equals(gameData.whiteUsername()) ? null : gameData.whiteUsername(),
                            username.equals(gameData.blackUsername()) ? null : gameData.blackUsername(),
                            gameData.gameName(),
                            gameData.game()
                    );
                    gameService.updateGame(updatedGame);
                }
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
            ChessGame game = gameData.game();
            if (game.isGameOver()) {
                sendError(session, "Error: Game is already over");
                return;
            }

            // Process valid resignation
            game.setGameOver(true);
            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            );
            gameService.updateGame(updatedGame);

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