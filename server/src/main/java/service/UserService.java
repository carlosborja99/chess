package service;
import dataaccess.*;

import model.UserData;
import model.AuthData;
import java.util.UUID;
public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public record RegisterRequest(String username, String password, String email) {}
    public record RegisterResponse(String authToken, String username) {}

    public RegisterResponse register(RegisterRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new DataAccessException("This is a bad request");
        }
        UserData user = new UserData(request.username, request.password, request.email);
        dataAccess.createUser(user);
        String authToken = generateToken();
        dataAccess.createAuthorization(new AuthData(authToken, request.username()));
        return new RegisterResponse(authToken, request.username());
    }
    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String username, String authToken) {}

    public LoginResponse login(LoginRequest request) throws DataAccessException {
        UserData user = dataAccess.getUser(request.username());
        if(user == null || !user.password().equals(request.password())){
            throw new DataAccessException("Error: unauthorized");
        }
        String authToken = generateToken();
        dataAccess.createAuthorization(new AuthData(authToken, request.username()));
        return new LoginResponse(request.username(), authToken);
    }
    public record LogoutRequest(String authToken) {}
    public void logout(LogoutRequest request) throws DataAccessException{
        AuthData authorized = dataAccess.getAuthorization(request.authToken());
        if(authorized == null){
            throw new DataAccessException("Error: unauthorized");
        }
        dataAccess.deleteAuthorization(request.authToken());
    }
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}

