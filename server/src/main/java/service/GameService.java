package service;
import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;

import java.util.Random;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public int createGame(String gameName) throws DataAccessException{
        GameData game = new GameData(new Random().nextInt(10000), null, null, gameName, new ChessGame());
        dataAccess.createGame(game);
        return game.gameID();
    }
    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException{
        dataAccess.getAuthorization(authToken);
        GameData game = dataAccess.getGame(gameID);
        if("WHITE".equals(playerColor) && game.whiteUsername() != null){
            throw new DataAccessException("Color is already taken");
        }
        if("BLACK".equals(playerColor) && game.whiteUsername() != null){
            throw new DataAccessException("Color is already taken");
        }
        if("WHITE".equals(playerColor)){
            game = new GameData(game.gameID(), authToken, game.blackUsername(), game.gameName(), game.game());
        }else{
            game = new GameData(game.gameID(), game.whiteUsername(), authToken, game.gameName(), game.game());
        }
        dataAccess.updateGame(game);
    }
}
