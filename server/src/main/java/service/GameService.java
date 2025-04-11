package service;
import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.List;

/**
 * Handles chess game operations like creating, joining and listing games.
 */
public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public record CreateGameRequest(String authToken, String gameName) {
    }

    public record CreateGameResult(int gameID) {
    }

    public record JoinRequest(String authToken, String playerColor, int gameID) {
    }

    public record JoinResult() {
    }

    public record ListGamesRequest(String authToken) {
    }

    public record ListGamesResult(List<GameData> games) {
    }

    /**
     * Clears all game-related data from the database
     *
     * @throws DataAccessException if the operation fails
     */
    public void clear() throws DataAccessException {
        dataAccess.clear();
    }

    /**
     * Create new chess game with the specified name.
     * @param request contains authentication token and game name.
     * @return the game result with the game ID.
     * @throws DataAccessException
     */
    public CreateGameResult createGame(CreateGameRequest request) throws DataAccessException {
        AuthData auth = validAuthorization(request.authToken());
        checkGameName(request.gameName);
        String gameName = request.gameName;
        int gameID = dataAccess.createGameID(gameName);
        GameData game = new GameData(gameID, null, null, request.gameName(), new ChessGame());
        dataAccess.updateGame(game);
        return new CreateGameResult(gameID);
    }


    public GameData getGame(int gameID) throws DataAccessException {
        return existingGame(gameID);
    }

    /**
     * Allows the player to join an existing game.
     *
     * @param request has the authToken, player color, and the game ID.
     * @return a result that shows a successful join
     * @throws DataAccessException if the request is invalid or the game spot is already taken.
     */
    public JoinResult joinGame(JoinRequest request) throws DataAccessException {
        if (request.gameID() <= 0) {
            throw new DataAccessException("Bad Request");
        }

        AuthData auth = validAuthorization(request.authToken());
        GameData game = existingGame(request.gameID());
        checkPlayerColor(request.playerColor());
        GameData upToDateGame = getGameData(request, game, auth.username());
        dataAccess.updateGame(upToDateGame);
        return new JoinResult();
    }

    private void checkGameName(String gameName) throws DataAccessException {
        if (gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Bad Request");
        }
    }

    private AuthData validAuthorization(String authToken) throws DataAccessException {
        AuthData authData = dataAccess.getAuthorization(authToken);
        if (authData == null) {
            throw new DataAccessException("Unauthorized");
        }
        return authData;
    }

    private GameData existingGame(int gameID) throws DataAccessException {
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }
        return game;
    }

    private void checkPlayerColor(String playerColor) throws DataAccessException {
        if (playerColor == null || playerColor.isEmpty()) {
            throw new DataAccessException("Bad Request");
        }
    }
    private GameData getGameData(JoinRequest request, GameData game, String username) throws DataAccessException {
        ChessGame.TeamColor teamColor = parseTeamColor(request.playerColor());
        if (teamColor == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("Already taken");
            }
            return new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        } else if (teamColor == ChessGame.TeamColor.BLACK) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Already taken");
            }
            return new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        } else {
            throw new DataAccessException("Bad Request");
        }
    }

    private ChessGame.TeamColor parseTeamColor(String teamColor) throws DataAccessException {
        try {
            return ChessGame.TeamColor.valueOf(teamColor.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DataAccessException("Bad Request");
        }
    }

    /**
     * Lists all the existing games.
     *
     * @param request contains the Authentication token.
     * @return the result containing a list with all the games
     * @throws DataAccessException
     */
    public ListGamesResult listGames(ListGamesRequest request) throws DataAccessException {
        AuthData authorized = validAuthorization(request.authToken());
        List<GameData> games = dataAccess.listOfGames();
        return new ListGamesResult(games);
    }

    public void updateGame(GameData game) throws DataAccessException {
        if (game == null || game.gameID() <= 0) {
            throw new DataAccessException("Bad Request");
        }
        dataAccess.updateGame(game);
        GameData updated = dataAccess.getGame(game.gameID());
        if (updated == null) {
            throw new DataAccessException("Game not found after update");
        }
        if (game.game().isGameOver() != updated.game().isGameOver()) {
            throw new DataAccessException("Game over state was not persisted correctly");
        }
    }
}

