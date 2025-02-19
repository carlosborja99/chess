package model;

import chess.ChessGame;

public class SharedModule {
    public record UserData(String username, String password, String email) { }
    public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) { }
    public record AuthData(String authToken, String username) { }
}
