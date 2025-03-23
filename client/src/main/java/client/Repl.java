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


}
