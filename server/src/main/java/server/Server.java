package server;

import spark.*;
import service.*;
import dataaccess.*;
public class Server {
    private UserService userService;
    private GameService gameService;
    public void server(){
        DataAccess dataAccess = new AccessDataInMemory();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
    }
    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
