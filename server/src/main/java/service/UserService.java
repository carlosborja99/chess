package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

/**
 * This program handles operations related to user authentication and registration.
 */
public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    /**
     * Clears all user data from the program.
     * @throws DataAccessException if clearing fails
     */
    public void clear() throws DataAccessException {
        dataAccess.clear();
    }

    /**
     * Request and response records when registering, Logging in, and Logging out, that last one does not need a response.
     * @param username is the username
     * @param password is the password
     * @param email is the email
     */
    public record RegisterRequest(String username, String password, String email) {}
    public record RegisterResponse(String authToken, String username) {}
    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String username, String authToken) {}
    public record LogoutRequest(String authToken) {}

    /**
     * Registers a new user with the provided parameters.
     * @param request is a request that provides a username, a password and an email.
     * @return returns an authentication token and a username.
     * @throws DataAccessException if the registration process fails.
     */
    public RegisterResponse register(RegisterRequest request) throws DataAccessException {
        checkRegistration(request);
        doesUserExist(request.username);
        String hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt());
        UserData newUser = new UserData(request.username, hashedPassword, request.email);

        dataAccess.createUser(newUser);
        String authToken = generateToken();
        dataAccess.createAuthorization(new AuthData(authToken, request.username()));

        return new RegisterResponse(authToken, request.username());
    }

    /**
     * Authenticates existing users and creates new session.
     * @param request containing a username and a password.
     * @return a response with the username & authentication token.
     * @throws DataAccessException if the login fails.
     */
    public LoginResponse login(LoginRequest request) throws DataAccessException {
        UserData user = dataAccess.getUser(request.username());
        loginCredentials(user, request.password());
        String authToken = generateToken();
        dataAccess.createAuthorization(new AuthData(authToken, request.username()));
        return new LoginResponse(request.username(), authToken);
    }

    /**
     * Terminates the user's session.
     * @param request with an authentication token
     * @throws DataAccessException if the logout fails
     */
    public void logout(LogoutRequest request) throws DataAccessException{
        AuthData authorized = dataAccess.getAuthorization(request.authToken());
        isAuthorized(authorized);
        dataAccess.deleteAuthorization(request.authToken());
    }
    private void doesUserExist(String username) throws DataAccessException {
        if (dataAccess.getUser(username) != null) {
            throw new DataAccessException("User already exists");
        }
    }

    private void checkRegistration(RegisterRequest request) throws DataAccessException {
        if (request.username == null || request.password() == null || request.email() == null) {
            throw new DataAccessException("Error: bad request");
        }
    }

    private void loginCredentials(UserData user, String password) throws DataAccessException {
        if (user == null ){
            throw new DataAccessException("Error: unauthorized");
        }
        System.out.println("Stored password: " + user.password());
        System.out.println("Provided password: " + password);
        System.out.println("BCrypt check: " + BCrypt.checkpw(password, user.password()));
        if(!BCrypt.checkpw(password, user.password())) {
            throw new DataAccessException("Error: unauthorized");
        }
    }

    private void isAuthorized(AuthData authData) throws DataAccessException {
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }
    }
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}