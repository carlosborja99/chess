package client;

import chess.*;
import ui.RenderBoard;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Scanner;

public class Repl {
    private final ServerFacade facade;
    private boolean logged = false;
    private Map<Integer, String> gameNumberToID = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);

    public Repl(String host) {
        this.facade = new ServerFacade(host);
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
        System.out.print(">>> ");
        String[] input = scanner.nextLine().split("\\s+");
        String command = input[0].toLowerCase();

        switch (command){
            case "help":
                System.out.println("Available commands:");
                System.out.println("  help - Display this help text");
                System.out.println("  quit - Exit the program");
                System.out.println("  login <username> <password> - Log in to your account");
                System.out.println("  register <username> <password> <email> - Create a new account");
                break;
            case "quit":
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            case "register":
                if(input.length != 4){
                    System.out.println("Usage: register <username> <password> <email>");
                    break;
                }
                Map<String, Object> registerResponse = facade.register(input[1], input[2], input[3]);
                System.out.println("Registered and logged in as " + input[1]);
                logged = true;
                break;
            case "login":
                if(input.length != 3){
                    System.out.println("Usage: login <username> <password>");
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
                System.out.println("  help - Display this help text");
                System.out.println("  logout - Log out of your account");
                System.out.println("  create <gameName> - Create a new game");
                System.out.println("  list - List all existing games");
                System.out.println("  play <gameNumber> <color> - Join a game as WHITE or BLACK");
                System.out.println("  observe <gameNumber> - Observe a game");
                break;
            case "logout":
                facade.logout();
                System.out.println("Logged out.");
                logged = false;
                break;
            case "create":
                if(input.length != 2){
                    System.out.println("Usage: create <gameName>");
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
                        String whitePlayer = game.get("whiteUsername").toString();
                        String blackPlayer = game.get("blackUsername").toString();
                        int num = i + 1;
                        gameNumberToID.put(num, gameID);
                        System.out.printf("%d. %s [White: %s] [Black: %s]%n",
                                num, gameName, whitePlayer != null ? whitePlayer : "None", blackPlayer != null ? blackPlayer : "None");
                    }
                }
                break;
            case "play":
                if(input.length != 3 || (!input[2].equalsIgnoreCase("WHITE") && !input[2].equalsIgnoreCase("BLACK"))){
                    System.out.println("Usage: play <gameNumber> <WHITE|BLACK>");
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
                new RenderBoard().render(input[2].equalsIgnoreCase("WHITE"));
                break;
            case "observe":
                if(input.length != 2){
                    System.out.println("Usage: observe <gameNumber>");
                    break;
                }
                int observeNum = Integer.parseInt(input[1]);
                String gameIDObserve = gameNumberToID.get(observeNum);
                if (gameIDObserve == null){
                    System.out.println("Invalid game number.");
                    break;
                }
                break;
            default:
                System.out.println("Unknown command. Type \"help\" for options.");

        }
    }
}
