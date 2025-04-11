package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

/**
 * Handles user authentication and registration operations.
 */
public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    /**
     * Clears all user data
     * @throws DataAccessException if it fails
     */
    public void clear() throws DataAccessException {
        dataAccess.clear();
    }

    // Request and response records for register, login and logout, logout does not require a response.
    public record RegisterRequest(String username, String password, String email) {}
    public record RegisterResponse(String authToken, String username) {}
    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String username, String authToken) {}
    public record LogoutRequest(String authToken) {}

    /**
     * Registers a new user with the provided credentials.
     * @param request containing username, password, and email
     * @return response with authentication token and username
     * @throws DataAccessException if registration fails
     */
    public RegisterResponse register(RegisterRequest request) throws DataAccessException {
        validateRegisterRequest(request);
        verifyUserDoesNotExist(request.username());
        String hashedPassword = hashPassword(request.password());
        UserData newUser = new UserData(request.username(), hashedPassword, request.email());

        dataAccess.createUser(newUser);
        String authToken = generateToken();
        dataAccess.createAuthorization(new AuthData(authToken, request.username()));

        return new RegisterResponse(authToken, request.username());
    }

    /**
     * Authenticates existing users and creates a new session for them.
     * @param request contains username and password.
     * @return a LoginResponse with Username and a authorization Token
     * @throws DataAccessException if the login fails
     */
    public LoginResponse login(LoginRequest request) throws DataAccessException {
        UserData user = getUserOrThrow(request.username());
        verifyPassword(user, request.password());

        String authToken = generateToken();
        dataAccess.createAuthorization(new AuthData(authToken, request.username()));

        return new LoginResponse(request.username(), authToken);
    }

    /**
     * Terminates the user's session.
     * @param request that contains the authentication token
     * @throws DataAccessException if the logout fails
     */
    public void logout(LogoutRequest request) throws DataAccessException {
        AuthData authData = getAuthorizationOrThrow(request.authToken());
        dataAccess.deleteAuthorization(authData.authToken());
    }

    // Here are the private helper methods
    private void validateRegisterRequest(RegisterRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new DataAccessException("Error: bad request");
        }
    }

    private void verifyUserDoesNotExist(String username) throws DataAccessException {
        if (dataAccess.getUser(username) != null) {
            throw new DataAccessException("User already exists");
        }
    }

    public AuthData verifyAuthToken(String authToken) throws DataAccessException {
        return getAuthorizationOrThrow(authToken);
    }

    private UserData getUserOrThrow(String username) throws DataAccessException {
        UserData user = dataAccess.getUser(username);
        if (user == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return user;
    }

    private void verifyPassword(UserData user, String password) throws DataAccessException {
        if (!BCrypt.checkpw(password, user.password())) {
            throw new DataAccessException("Error: unauthorized");
        }
    }

    private AuthData getAuthorizationOrThrow(String authToken) throws DataAccessException {
        AuthData authData = dataAccess.getAuthorization(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return authData;
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}