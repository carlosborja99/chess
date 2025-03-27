package service;
import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;
    private AccessDataInMemory dataInMemory;

    @BeforeEach
    void setUp(){
        dataInMemory = new AccessDataInMemory();
        userService = new UserService(dataInMemory);

    }
    @Test
    void registerSuccess() throws DataAccessException{
        var request = new UserService.RegisterRequest("user1", "pass", "email@example.com");
        var result = userService.register(request);
        assertEquals("user1", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void registerDuplicateUser(){
        var request = new UserService.RegisterRequest("user1", "pass", "email@example.com");
        assertDoesNotThrow(() -> userService.register(request));
        assertThrows(DataAccessException.class, () -> userService.register(request));
    }
    @Test
    void registerWithNullUsername(){
        var nullUserNameRequest = new UserService.RegisterRequest(null, "pass", "email@example.com");
        assertThrows(DataAccessException.class, () -> userService.register(nullUserNameRequest));
    }
    @Test
    void registerWithNullPassword(){
        var nullPasswordRequest = new UserService.RegisterRequest("user1", null, "email@example.com");
        assertThrows(DataAccessException.class, () -> userService.register(nullPasswordRequest));
    }
    @Test
    void registerWithNullEmail(){
        var nullEmailRequest = new UserService.RegisterRequest("user1", "pass", null);
        assertThrows(DataAccessException.class, () -> userService.register(nullEmailRequest));
    }
    @Test
    void loginSuccess() throws DataAccessException{
        var registerRequest = new UserService.RegisterRequest("user1", "pass", "email@example.com");
        userService.register(registerRequest);
        var loginRequest = new UserService.LoginRequest(registerRequest.username(), registerRequest.password());
        var result = userService.login(loginRequest);
        assertEquals("user1", result.username());
        assertNotNull(result.authToken());
        AuthData authorizationData = dataInMemory.getAuthorization(result.authToken());
        assertNotNull(authorizationData);
        assertEquals("user1", authorizationData.username());
    }
    @Test
    void loginWithWrongPassword() throws DataAccessException{
        var registerRequest = new UserService.RegisterRequest("user1", "pass", "email@example.com");
        userService.register(registerRequest);
        var wrongPassword = new UserService.LoginRequest(registerRequest.username(), "wrongPassword");
        assertThrows(DataAccessException.class, () -> userService.login(wrongPassword));
    }
    @Test
    void logout() throws DataAccessException {
        var registerRequest = new UserService.RegisterRequest("user1", "pass", "email@example.com");
        var registerResult = userService.register(registerRequest);
        String authorizationToken = registerResult.authToken();
        assertNotNull(dataInMemory.getAuthorization(authorizationToken));
        var logoutRequest = new UserService.LogoutRequest(authorizationToken);
        assertDoesNotThrow(() -> userService.logout(logoutRequest));
        assertThrows(DataAccessException.class, () -> userService.logout(logoutRequest));
    }
}
