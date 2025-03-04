package service;
import dataaccess.*;
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

}
