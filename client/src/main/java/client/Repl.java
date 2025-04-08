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

        switch (command){
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
                if(input.length != 4){
                    System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL> - to create an account");
                    break;
                }
                Map<String, Object> registerResponse = facade.register(input[1], input[2], input[3]);
                System.out.println("Registered and logged in as " + input[1]);
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
                System.out.println("  logout - when you are done");
                System.out.println("  help - with possible commands\n");
                break;
            case "logout":
                facade.logout();
                System.out.println("Logged out.");
                logged = false;
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
                new RenderBoard().render(input[2].equalsIgnoreCase("WHITE"));
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
                connectWebSocket(); // Initiate WebSocket connection after observing
                boardRender.render(true); // Observer always sees from white's perspective initially
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
}
