package service;
import chess.ChessGame;
import dataaccess.*;
import model.*;

import java.util.ArrayList;
import java.util.List;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public void clear() throws DataAccessException {
        dataAccess.clear();
    }
    public record createGameRequest(String authToken, String gameName){}
    public record createGameResult(int gameID){}

    public createGameResult createGame(createGameRequest request) throws DataAccessException{
        AuthData auth = dataAccess.getAuthorization(request.authToken());
        if(auth == null){
            throw new DataAccessException("Unauthorized");
        }if (request.gameName() == null || request.gameName().isEmpty()){
            throw new DataAccessException("Bad Request");
        }
        int gameID = dataAccess.createGameID();
        GameData game = new GameData(gameID, null, null, request.gameName(), new ChessGame());
        dataAccess.createGame(game);
        return new createGameResult(gameID);
    }

    public record JoinRequest(String authToken, String playerColor, int gameID){}
    public record JoinResult(){}
    public JoinResult joinGame(JoinRequest request) throws DataAccessException{
        if (request.gameID() <= 0){
            throw new DataAccessException("Bad Request");
        }
        AuthData auth = dataAccess.getAuthorization(request.authToken());
        if(auth == null){
            throw new DataAccessException("Unauthorized");
        }
        GameData game = dataAccess.getGame(request.gameID());
        if(game == null){
            throw new DataAccessException("Game not found");
        }
        String username = auth.username();

        if (request.playerColor() == null || request.playerColor().isEmpty()){
            throw new DataAccessException("Bad Request");
        }
        ChessGame.TeamColor teamColor;
        try{
            teamColor = ChessGame.TeamColor.valueOf(request.playerColor().toUpperCase());
        }catch (IllegalArgumentException e){
            throw new DataAccessException("Bad Request");
        }
        GameData game2;
        if(teamColor == ChessGame.TeamColor.WHITE){
            if(game.whiteUsername() != null){
                throw new DataAccessException("Already taken");
            }
            game2 = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        } else if(teamColor == ChessGame.TeamColor.BLACK){
            if(game.blackUsername() != null){
                throw new DataAccessException("Already taken");
            }
            game2 = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        }else{
            throw new DataAccessException("Bad Request");
        }
        dataAccess.updateGame(game2);
        return new JoinResult();
    }

    private static GameData getGameData(JoinRequest request, GameData game, String username) throws DataAccessException {
        GameData game2;
        ChessGame.TeamColor teamColor;
        try{
            teamColor = ChessGame.TeamColor.valueOf(request.playerColor().toUpperCase());
        }catch (IllegalArgumentException e){
            throw new DataAccessException("Bad Request");
        }        if(teamColor == ChessGame.TeamColor.WHITE){
            if(game.whiteUsername() != null){
                throw new DataAccessException("Already taken");
            }
            game2 = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        } else if(teamColor == ChessGame.TeamColor.BLACK){
            if(game.blackUsername() != null){
                throw new DataAccessException("Already taken");
            }
            game2 = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        }else{
            throw new DataAccessException("Bad Request");
        }
        return game2;
    }

    public record ListGamesRequest(String authToken){}
    public record ListGamesResult(List<GameData> games){}

    public ListGamesResult listGames(ListGamesRequest request) throws DataAccessException{
        AuthData authorized = dataAccess.getAuthorization(request.authToken());
        if(authorized == null){
            throw new DataAccessException("Unauthorized");
        }
        List<GameData> games = dataAccess.listOfGames();
        return new ListGamesResult(games);
    }
}
