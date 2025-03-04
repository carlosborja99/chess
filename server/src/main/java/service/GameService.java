package service;
import chess.ChessGame;
import dataaccess.*;
import model.*;

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
        }if (request.gameName() == null){
            throw new DataAccessException("Bad Request");
        }
        int gameID = Integer.parseInt(request.gameName);
        GameData game = new GameData(gameID, null, null, request.gameName(), new ChessGame());
        dataAccess.createGame(game);
        return new createGameResult(gameID);
    }
    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException{
        AuthData auth = dataAccess.getAuthorization(authToken);
        if(auth == null){
            throw new DataAccessException("Unauthorized");
        }
        GameData game = dataAccess.getGame(gameID);
        if(game == null){
            throw new DataAccessException("Game not found");
        }

        if("WHITE".equals(playerColor) && game.whiteUsername() != null){
            throw new DataAccessException("White color is already taken");
        }
        if("BLACK".equals(playerColor) && game.whiteUsername() != null){
            throw new DataAccessException("Black color is already taken");
        }
        if("WHITE".equals(playerColor)){
            game = new GameData(game.gameID(), authToken, game.blackUsername(), game.gameName(), game.game());
        }else if ("BLACK".equals(playerColor)){
            game = new GameData(game.gameID(), game.whiteUsername(), authToken, game.gameName(), game.game());
        }else{
            throw new DataAccessException("Bad Request");
        }
        dataAccess.updateGame(game);
    }
}
