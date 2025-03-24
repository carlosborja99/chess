package client;

import chess.*;

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

    public void run(){
        System.out.println("♕ 240 Chess Client: Welcome!");
        while (true){
            try{
                if (logged){
                    preLogin();
                } else{
                    postLogin();
                }
            } catch (Exception e){
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    private void preLogin() throws Exception{
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
            case "quit":
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            case "register":
                if(input.length != 4){
                    System.out.println("Usage: register <username> <password> <email>");
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

}
