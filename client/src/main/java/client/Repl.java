package client;

import chess.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.glassfish.tyrus.client.ClientManager;
import ui.RenderBoard;
import websocket.commands.UserGameCommand;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ui.RenderBoard;
import websocket.commands.UserGameCommand;

import javax.websocket.*;
import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Repl {
    private final ServerFacade facade;
    private boolean logged = false;
    private Map<Integer, String> gameNumberToID = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);
    private Session websocketSession;
    private String currentGameID;
    private ChessGame.TeamColor playerColor;
    private ChessBoard currentBoard;
    private RenderBoard boardRender = new RenderBoard();
    private String username;
    private boolean observe = false;
    private Gson gson = new Gson();

    public Repl(String host) {
        this.facade = new ServerFacade(host);
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("[Now connected to websocket] Session ID: " + session.getId());
        this.websocketSession = session;
        sendConnectCommand();
    }

    @OnMessage
    public void onMessage(String message) {
        Gson gson = new Gson();
        JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
        if (jsonMessage.has("serverMessageType")) {
            String tipe = jsonMessage.get("serverMessageType").getAsString();
            JsonObject data = jsonMessage.getAsJsonObject("data");
            switch (tipe) {
                case "LOAD_GAME":
                    if (data.has("game")) {
                        JsonObject gameData = data.getAsJsonObject("game");
                        String boardPlay = gameData.has("board") ? gameData.get("board").getAsString() : null;
                        if (boardPlay != null) {
                            currentBoard = FenUtility.FENtoBoard(boardPlay);
                            boardRender.render(playerColor == ChessGame.TeamColor.WHITE || observe, currentBoard);
                        } else {
                            System.out.println("LOAD_GAME received without board information.");
                            System.out.println("Raw message: " + message);
                        }
                    } else {
                        System.out.println("LOAD_GAME received without game data.");
                        System.out.println("Raw message: " + message);
                    }
                    break;
                case "ERROR":
                    if (data.has("errorMessage")) {
                        String errorMessage = data.get("errorMessage").getAsString();
                        System.err.println("Server Error: " + errorMessage);
                    }
                    break;
                case "NOTIFICATION":
                    if (data.has("message")) {
                        String notificationMessage = data.get("message").getAsString();
                        System.out.println("Notification: " + notificationMessage);
                    }
                    break;
                default:
                    System.out.println("Received unknown server message type: " + tipe);
                    System.out.println("Raw message: " + message);
            }
        } else {
            System.out.println("Received message without serverMessageType: " + message);
        }

    }


    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("[WebSocket Error] Session ID: " + session.getId() + " Error: " + throwable.getMessage());
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("[WebSocket Closed] Session ID: " + session.getId() + " Reason: " + closeReason.getReasonPhrase());
        websocketSession = null;
    }

    public void run() {
        System.out.println("♕ Welcome! to 240 Chess. Type help to get started. ♕");
        while (true){
            try{
                if (logged){
                    postLogin();
                } else{
                    preLogin();
                }
            } catch (Exception e){
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    private void preLogin() throws Exception {
        System.out.print("[LOGGED_OUT] >>> ");
        String[] input = scanner.nextLine().split("\\s+");
        String command = input[0].toLowerCase();

        switch (command) {
            case "help":
                System.out.println("  register <USERNAME> <PASSWORD> <EMAIL> - to create an account");
                System.out.println("  login <USERNAME> <PASSWORD> - to play chess");
                System.out.println("  quit - playing chess");
                System.out.println("  help - with possible commands\n");
                break;
            case "quit":
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            case "register":
                if (input.length != 4) {
                    System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL> - to create an account");
                    break;
                }
                Map<String, Object> registerResponse = facade.register(input[1], input[2], input[3]);
                System.out.println("Registered and logged in as " + input[1]);
                username = input[1];
                logged = true;
                break;
            case "login":
                if(input.length != 3){
                    System.out.println("Usage: login <USERNAME> <PASSWORD> - to play chess");
                    break;
                }
                Map<String, Object> loginResponse = facade.login(input[1], input[2]);
                System.out.println("Logged in as " + input[1]);
                logged = true;
                username = input[1];
                break;
            case "logout":
                break;
            default:
                System.out.println("Unknown command. Type \"help\" for options.");
        }
    }
    private void postLogin() throws Exception {
        System.out.print("[LOGGED_IN] >>> ");
        String[] input = scanner.nextLine().split("\\s+");
        String command = input[0].toLowerCase();
        switch (command){
            case "help":
                System.out.println("Available commands:");
                System.out.println("  create <NAME> - a game");
                System.out.println("  list - games");
                System.out.println("  join <ID> <WHITE|BLACK> - a game");
                System.out.println("  observe <ID> - a game");
                System.out.println("  move <start> <end> - make a move (e.g., a2 a4)");
                System.out.println("  leave - the current game");
                System.out.println("  resign - the current game");
                System.out.println("  logout - when you are done");
                System.out.println("  help - with possible commands\n");
                break;
            case "logout":
                facade.logout();
                System.out.println("Logged out.");
                logged = false;
                currentGameID = null;
                playerColor = null;
                observe = false;
                if (websocketSession.isOpen() && websocketSession != null) {
                    websocketSession.close();
                    websocketSession = null;
                }
                break;
            case "create":
                if(input.length != 2){
                    System.out.println("Usage: create <NAME>");
                    break;
                }
                facade.createMyGame(input[1]);
                System.out.println("Game " + input[1] + " created.");
                break;
            case "list":
                List<Map<String, Object>> games = facade.listOfGames();
                gameNumberToID.clear();
                if (games.isEmpty()){
                    System.out.println("No games available.");
                } else {
                    for (int i = 0; i < games.size(); i++){
                        Map<String, Object> game = games.get(i);
                        String gameID = game.get("gameID").toString();
                        String gameName = game.get("gameName").toString();
                        String whitePlayer = game.get("whiteUsername") != null ? game.get("whiteUsername").toString() : "None";
                        String blackPlayer = game.get("blackUsername") != null ? game.get("blackUsername").toString() : "None";
                        int num = i + 1;
                        gameNumberToID.put(num, gameID);
                        System.out.printf("%d. %s [White: %s] [Black: %s]%n",
                                num, gameName, whitePlayer, blackPlayer);
                    }
                }
                break;
            case "join":
                if(input.length != 3 || (!input[2].equalsIgnoreCase("WHITE") && !input[2].equalsIgnoreCase("BLACK"))){
                    System.out.println("Usage: join <ID> <WHITE|BLACK>");
                    break;
                }
                int playNum = Integer.parseInt(input[1]);
                String gameID = gameNumberToID.get(playNum);
                if (gameID == null){
                    System.out.println("Invalid game number.");
                    break;
                }
                facade.joinGame(gameID, input[2].toUpperCase());
                System.out.println("Joined game as " + input[2]);
                playerColor = input[2].equalsIgnoreCase("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                currentGameID = gameID;
                observe = false;
                connectWebSocket(); // Initiate WebSocket connection after joining
//                new RenderBoard().render(input[2].equalsIgnoreCase("WHITE"));
                break;
            case "observe":
                if(input.length != 2){
                    System.out.println("Usage: observe <ID>");
                    break;
                }
                int observeNum = Integer.parseInt(input[1]);
                String gameIDObserve = gameNumberToID.get(observeNum);
                if (gameIDObserve == null){
                    System.out.println("Invalid game number.");
                    break;
                }
                System.out.println("Observing game " + observeNum);
                currentGameID = gameIDObserve;
                observe = true;
                connectWebSocket();
                boardRender.render(true); // Observer always sees from white's perspective initially
                break;
            case "move":
                if(input.length != 3){
                    System.out.println("Usage: move <start_square> <end_square>");
                    break;
                }
                if (currentGameID != null && websocketSession.isOpen() && websocketSession != null) {
                    try {
                        ChessPosition startPosition = notationToPosition(input[1]);
                        ChessPosition endPosition = notationToPosition(input[2]);
                        if (startPosition != null && endPosition != null){
                            ChessMove move = new ChessMove(startPosition, endPosition, null);
                            UserGameCommand makeMoveCommand = new UserGameCommand(
                                    UserGameCommand.CommandType.MAKE_MOVE,
                                    facade.getAuthToken(),
                                    Integer.parseInt(currentGameID)
                            );
                            Map<String, Object> moveData = new HashMap<>();
                            moveData.put("start", positionToObject(startPosition));
                            moveData.put("end", positionToObject(endPosition));
                            JsonObject jsonCommand = new JsonObject();
                            jsonCommand.addProperty("commandType", "MAKE_MOVE");
                            jsonCommand.addProperty("authToken", facade.getAuthToken());
                            jsonCommand.addProperty("gameID", currentGameID);
                            jsonCommand.add("move", gson.toJsonTree(moveData));
                            websocketSession.getBasicRemote().sendText(gson.toJson(jsonCommand));
                            System.out.println("Sent move: " + input[1] + " to " + input[2]);
                        } else {
                            System.out.println("Invalid move notation!");
                        }
                    } catch (IOException e) {
                        System.err.println("Error sending move: " + e.getMessage());
                    }
                } else {
                    System.out.println("Not connected via WebSocket");
                }
                break;
            case "leave":
                if (websocketSession != null && currentGameID != null && websocketSession.isOpen()) {
                    sendWebSocketCommand(UserGameCommand.CommandType.LEAVE);
                    currentGameID = null;
                    playerColor = null;
                    observe = false;
                    System.out.println("Left the game");
                } else{
                    System.out.println("No game to leave");
                }
                break;
            case "resign":
                if (websocketSession != null && currentGameID != null && websocketSession.isOpen()) {
                    sendWebSocketCommand(UserGameCommand.CommandType.RESIGN);
                    System.out.println("Resigned from game");
                    currentGameID = null;
                    playerColor = null;
                    observe = false;
                } else{
                    System.out.println("No game to resign from");
                }
                break;
            default:
                System.out.println("Unknown command. Type \"help\" for options.");

        }
    }

    private void connectWebSocket() {
        ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
        try {
            ClientManager client = ClientManager.createClient();
            client.connectToServer(this, new URI(facade.getHost() + "/ws"));
        } catch (DeploymentException | IOException | URISyntaxException e) {
            System.err.println("Error connecting to WebSocket: " + e.getMessage());
        }
    }

    private void sendConnectCommand() {
        if (websocketSession != null && websocketSession.isOpen() && logged && currentGameID != null) {
            UserGameCommand command = new UserGameCommand(
                    UserGameCommand.CommandType.CONNECT,
                    facade.getAuthToken(),
                    Integer.parseInt(currentGameID)
            );
            String jsonCommand = gson.toJson(command);
            try {
                websocketSession.getBasicRemote().sendText(jsonCommand);
                System.out.println("Sent CONNECT message.");
            } catch (IOException e) {
                System.err.println("Error sending CONNECT message: " + e.getMessage());
            }
        }
    }

    private ChessPosition notationToPosition(String notation) {
        if (notation.length() != 2) {
            return null;
        }
        char fileCharacter = notation.charAt(0);
        char rankCharacter = notation.charAt(1);
        if (rankCharacter >= '1' && rankCharacter <= '8' && fileCharacter >= 'a' && fileCharacter <= 'h'){
            int column = fileCharacter - 'a';
            int row = Integer.parseInt(String.valueOf(rankCharacter)) - 1;
            return new ChessPosition(row + 1, column + 1);
        }
        return null;
    }

    private Map<String, Integer> positionToObject(ChessPosition position) {
        Map<String, Integer> object = new HashMap<>();
        object.put("row", position.getRow());
        object.put("col", position.getColumn());
        return object;
    }

    private void sendWebSocketCommand(UserGameCommand.CommandType type) {
        if (websocketSession != null && websocketSession.isOpen() && logged && currentGameID != null) {
            UserGameCommand command = new UserGameCommand(
                    type,
                    facade.getAuthToken(),
                    Integer.parseInt(currentGameID)
            );
            String jsonCommand = gson.toJson(command);
            try {
                websocketSession.getBasicRemote().sendText(jsonCommand);
                System.out.println("Sent " + type + " command.");
            } catch (IOException e) {
                System.err.println("Error sending " + type + " command: " + e.getMessage());
            }
        } else {
            System.out.println("Not not logged in or not connected to a game via WebSocket.");
        }
    }
}
