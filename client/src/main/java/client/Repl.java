package client;

import chess.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.glassfish.tyrus.client.ClientManager;
import ui.RenderBoard;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@ClientEndpoint
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
    private Map<String, ChessPosition> highlightedMoves = new HashMap<>();

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
        try {
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
            switch (serverMessage.getServerMessageType()) {
                case LOAD_GAME:
                    ChessGame chessGame = serverMessage.getGame();
                    if (chessGame != null) {
                        currentBoard = chessGame.getBoard();
                        boardRender.render(currentBoard, playerColor == ChessGame.TeamColor.WHITE || observe, highlightedMoves);
                    } else {
                        System.err.println("LOAD_GAME message missing game data");
                    }
                    break;
                case ERROR:
                    String errorMsg = serverMessage.getErrorMessage() != null ? serverMessage.getErrorMessage() : serverMessage.getMessage();
                    System.out.println("Error: " + (errorMsg != null ? errorMsg : "Unknown error"));
                    break;
                case NOTIFICATION:
                    System.out.println("Notification: " + serverMessage.getMessage());
                    break;
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Error parsing message: " + e.getMessage());
            System.err.println("Raw message: " + message);
        } catch (Exception e) {
            System.err.println("Unexpected error processing message: " + e.getMessage());
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
        System.out.print(currentGameID == null ? "[LOGGED_IN] >>> " : "[IN_GAME] >>> ");
        String[] input = scanner.nextLine().split("\\s+");
        if (input.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String command = input[0].toLowerCase();

        switch (command) {
            case "help":
                displayPostLoginHelp();
                break;
            case "logout":
                handleLogout();
                break;
            case "create":
                handleCreateGame(input);
                break;
            case "list":
                handleListGames();
                break;
            case "join":
                handleJoinGame(input);
                break;
            case "observe":
                handleObserveGame(input);
                break;
            case "redraw":
                handleRedrawBoard();
                break;
            case "move":
                handleMakeMove(input);
                break;
            case "leave":
                handleLeaveGame();
                break;
            case "resign":
                handleResignGame();
                break;
            case "highlight":
                handleHighlightMoves(input);
                break;
            default:
                System.out.println("Unknown command. Type \"help\" for options.");
        }
    }

    private void displayPostLoginHelp() {
        if (currentGameID == null) {
            System.out.println("Available commands:");
            System.out.println("  create <NAME> - a game");
            System.out.println("  list - games");
            System.out.println("  join <ID> <WHITE|BLACK> - a game");
            System.out.println("  observe <ID> - a game");
            System.out.println("  logout - when you are done");
            System.out.println("  help - with possible commands\n");
        } else {
            System.out.println("Available commands:");
            System.out.println("  redraw - the chess board:");
            System.out.println("  move <start> <end> - make a move (e.g., a2 a4)");
            System.out.println("  leave - the current game");
            System.out.println("  resign - the current game");
            System.out.println("  highlight - the legal moves for a piece");
            System.out.println("  help - with possible commands\n");
        }
    }

    private void handleLogout() throws Exception {
        facade.logout();
        System.out.println("Logged out.");
        logged = false;
        currentGameID = null;
        playerColor = null;
        observe = false;
        highlightedMoves.clear();
        if (websocketSession != null && websocketSession.isOpen()) {
            websocketSession.close();
            websocketSession = null;
        }
    }

    private void handleCreateGame(String[] input) throws Exception {
        if (input.length != 2) {
            System.out.println("Usage: create <NAME>");
            return;
        }
        facade.createMyGame(input[1]);
        System.out.println("Game " + input[1] + " created.");
    }

    private void handleListGames() throws Exception {
        List<Map<String, Object>> games = facade.listOfGames();
        gameNumberToID.clear();
        if (games.isEmpty()) {
            System.out.println("No games available.");
        } else {
            for (int i = 0; i < games.size(); i++) {
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
    }
    private void handleJoinGame(String[] input) throws Exception {
        if (input.length != 3 || (!input[2].equalsIgnoreCase("WHITE") && !input[2].equalsIgnoreCase("BLACK"))) {
            System.out.println("Usage: join <ID> <WHITE|BLACK>");
            return;
        }
        int playNum = Integer.parseInt(input[1]);
        String gameID = gameNumberToID.get(playNum);
        if (gameID == null) {
            System.out.println("Invalid game number.");
            return;
        }
        facade.joinGame(gameID, input[2].toUpperCase());
        System.out.println("Joined game as " + input[2]);
        playerColor = input[2].equalsIgnoreCase("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        currentGameID = gameID;
        observe = false;
        highlightedMoves.clear();
        connectWebSocket();
    }

    private void handleObserveGame(String[] input) throws Exception {
        if (input.length != 2) {
            System.out.println("Usage: observe <ID>");
            return;
        }
        int observeNum = Integer.parseInt(input[1]);
        String gameIDObserve = gameNumberToID.get(observeNum);
        if (gameIDObserve == null) {
            System.out.println("Invalid game number.");
            return;
        }
        System.out.println("Observing game " + observeNum);
        currentGameID = gameIDObserve;
        observe = true;
        playerColor = null;
        highlightedMoves.clear();
        connectWebSocket();
        boardRender.render(currentBoard, playerColor == ChessGame.TeamColor.WHITE || observe, highlightedMoves);
    }

    private void handleRedrawBoard() {
        if (currentBoard != null) {
            boardRender.render(currentBoard, playerColor == ChessGame.TeamColor.WHITE || observe, highlightedMoves);
        } else {
            System.out.println("No board to redraw.");
        }
    }

    private void handleMakeMove(String[] input) throws IOException {
        if (input.length != 3) {
            System.out.println("Usage: move <start_square> <end_square>");
            return;
        }
        if (currentGameID != null && websocketSession != null && websocketSession.isOpen()) {
            try {
                ChessPosition startPosition = notationToPosition(input[1]);
                ChessPosition endPosition = notationToPosition(input[2]);
                if (startPosition != null && endPosition != null) {
                    ChessMove move = new ChessMove(startPosition, endPosition, null);
                    JsonObject jsonCommand = new JsonObject();
                    jsonCommand.addProperty("commandType", "MAKE_MOVE");
                    jsonCommand.addProperty("authToken", facade.getAuthToken());
                    jsonCommand.addProperty("gameID", currentGameID);
                    JsonObject moveData = new JsonObject();
                    moveData.add("start", gson.toJsonTree(positionToObject(startPosition)));
                    moveData.add("end", gson.toJsonTree(positionToObject(endPosition)));
                    jsonCommand.add("move", moveData);
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
    }

    private void handleLeaveGame() throws IOException {
        if (websocketSession != null && currentGameID != null && websocketSession.isOpen()) {
            sendWebSocketCommand(UserGameCommand.CommandType.LEAVE);
            currentGameID = null;
            playerColor = null;
            observe = false;
            highlightedMoves.clear();
            System.out.println("Left the game");
        } else {
            System.out.println("No game to leave");
        }
    }

    private void handleResignGame() throws IOException {
        System.out.print("Are you sure you want to resign from the game? (type 'yes' to confirm): ");
        String confirmation = scanner.nextLine().toLowerCase();
        if (confirmation.equals("yes")) {
            if (websocketSession != null && currentGameID != null && websocketSession.isOpen()) {
                sendWebSocketCommand(UserGameCommand.CommandType.RESIGN);
                System.out.println("Resigned from game");
                currentGameID = null;
                playerColor = null;
                observe = false;
                highlightedMoves.clear();
            } else {
                System.out.println("No game to resign from");
            }
        } else {
            System.out.println("Resignation cancelled.");
        }
    }

    private void handleHighlightMoves(String[] input) {
        if (input.length != 3) {
            System.out.println("Usage: highlight legal moves <square>");
            return;
        }
        if (currentBoard == null) {
            System.out.println("No board loaded.");
            return;
        }
        ChessPosition position = notationToPosition(input[2]);
        if (position == null) {
            System.out.println("Invalid square notation: " + input[2]);
            return;
        }
        ChessPiece piece = currentBoard.getPiece(position);
        if (piece == null || (!observe && piece.getTeamColor() != playerColor)) {
            System.out.println("No piece at " + input[2] + " or it's not your piece.");
            return;
        }
        ChessGame game = new ChessGame();
        game.setBoard(currentBoard);
        Set<ChessMove> legalMoves = (Set<ChessMove>) game.validMoves(position);
        highlightedMoves.clear();
        highlightedMoves.put(input[2], position);
        for (ChessMove move : legalMoves) {
            highlightedMoves.put(positionToNotation(move.getEndPosition()), move.getEndPosition());
        }
        boardRender.render(currentBoard, playerColor == ChessGame.TeamColor.WHITE || observe, highlightedMoves);
    }

    private String positionToNotation(ChessPosition endPosition) {
        char file = (char) ('a' + endPosition.getColumn() - 1);
        int rank = endPosition.getRow();
        return String.valueOf(file) + rank;

    }

    private void connectWebSocket() {
        ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
        try {
            ClientManager client = ClientManager.createClient();
            String wsUrl = facade.getHost().replace("http://", "ws://") + "ws";
            client.connectToServer(this, new URI(wsUrl));
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