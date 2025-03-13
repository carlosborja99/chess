package server;

import com.google.gson.Gson;
import dataaccess.*;
import service.*;
import spark.*;

public class Server {
    private final UserService userService;
    private final GameService gameService;
    private final Gson gson;

    public Server() {
//        DataAccess dataAccess = new AccessDataInMemory();
        MySQLDataAccess dataAccess = new MySQLDataAccess();
        try{
            dataAccess.configureDatabase();
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
        }
        this.userService = new UserService(dataAccess);
        this.gameService = new GameService(dataAccess);
        this.gson = new Gson();
    }

    public int run(int desiredPort) {

        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", this::clear);
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.get("/game", this::listGames);

        Spark.exception(DataAccessException.class, this::exceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }


    private Object clear(Request request, Response response) {
        try {
            gameService.clear();
            userService.clear();
            response.status(200);
            return "{}";
        } catch (DataAccessException e) {
            return errorResponse(response, 500, e.getMessage());
        }
    }

    private Object listGames(Request request, Response response) {
        try{
            String authToken = request.headers("Authorization");
            GameService.ListGamesResult listGamesResult = gameService.listGames(new GameService.ListGamesRequest(authToken));
            response.status(200);
            return gson.toJson(listGamesResult);
        }catch(DataAccessException e){
            return errorResponse(response, switch (e.getMessage()) {
                case "Unauthorized" -> 401;
                default -> 500;
            }, e.getMessage());
        }
    }

    private Object joinGame(Request request, Response response) {
        try{
            String auth = request.headers("Authorization");
            GameService.JoinRequest joinRequest = gson.fromJson(request.body(), GameService.JoinRequest.class);
            GameService.JoinRequest updatedJoinRequest = new GameService.JoinRequest(
              auth, joinRequest.playerColor(), joinRequest.gameID()
            );

            GameService.JoinResult joinResult = gameService.joinGame(updatedJoinRequest);
            response.status(200);
            return gson.toJson(joinResult);
        }catch(DataAccessException e){
            return errorResponse(response, switch (e.getMessage()) {
                case "Bad Request" -> 400;
                case "Unauthorized" -> 401;
                case "Already taken" -> 403;
                case "Game not found" -> 404;
                default -> 500;
            }, e.getMessage());
        }
    }


    private Object createGame(Request request, Response response) {
        try{
            String authToken = request.headers("Authorization");
            GameService.CreateGameRequest gameRequest = gson.fromJson(request.body(), GameService.CreateGameRequest.class);
            GameService.CreateGameResult gameResult = gameService.createGame(new GameService.CreateGameRequest(authToken, gameRequest.gameName()));
            response.status(200);
            return gson.toJson(gameResult);
        }catch(DataAccessException e){
            return errorResponse(response, switch(e.getMessage()) {
                case "Bad request" -> 400;
                case "Unauthorized" -> 401;
                case "User already exists" -> 403;
                default -> 500;
            }, e.getMessage());
        }
    }

    private Object logout(Request request, Response response) {
        try{
            String authToken = request.headers("Authorization");
            userService.logout(new UserService.LogoutRequest(authToken));
            response.status(200);
            return "{}";
        }catch(DataAccessException e){
            return errorResponse(response,401, e.getMessage());
        }
    }

    private Object login(Request request, Response response) {
        try {
            UserService.LoginRequest requestData = gson.fromJson(request.body(), UserService.LoginRequest.class);
            UserService.LoginResponse responseData = userService.login(requestData);
            response.status(200);
            return gson.toJson(responseData);
        } catch (DataAccessException e) {
            return errorResponse(response,401, e.getMessage());
        }
    }

    private Object register(Request request, Response response) {
        try {
            UserService.RegisterRequest registerRequest = gson.fromJson(request.body(), UserService.RegisterRequest.class);
            UserService.RegisterResponse registerResponse = userService.register(registerRequest);
            response.status(200);
            return gson.toJson(registerResponse);
        } catch (DataAccessException e) {
            return errorResponse(response,
                    e.getMessage().equals("User already exists") ? 403 : 400, e.getMessage());
        }
    }

    private void exceptionHandler(DataAccessException e, Request request, Response response) {
        response.status(500);
        response.body(gson.toJson(new ErrorResponse("Error: " + e.getMessage())));
    }

    private Object errorResponse(Response response, int status, String message) {
        response.status(status);
        return gson.toJson(new ErrorResponse("Error: " + message));
    }
    record ErrorResponse (String message) {}

}