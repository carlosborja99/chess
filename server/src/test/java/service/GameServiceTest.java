package service;
import chess.ChessGame;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
public class GameServiceTest {
    private GameService gameService;
    private AccessDataInMemory dataInMemory;
    private UserService userService;
    private String authToken;
    @BeforeEach
    void setUp() throws DataAccessException {
        dataInMemory = new AccessDataInMemory();
        userService = new UserService(dataInMemory);
        gameService = new GameService(dataInMemory);
        UserService.RegisterRequest registerRequest = new UserService.RegisterRequest("userTest", "approved", "mytest@example.com");
        UserService.RegisterResponse registerResponse = userService.register(registerRequest);
        authToken = registerResponse.authToken();
    }
    @Test
    void invalidGameID(){
        GameService.JoinRequest request = new GameService.JoinRequest(authToken, "WHITE", 9999);
        assertThrows(DataAccessException.class, () -> gameService.joinGame(request));
    }
}
