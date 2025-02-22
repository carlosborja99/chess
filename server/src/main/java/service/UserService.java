package service;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import java.util.UUID;
public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(String username, String password, String email) throws DataAccessException {
        UserData user = new UserData(username, password, email);
        dataAccess.createUser(user);
        String authToken = generateToken();
        AuthData authData = new AuthData(authToken, username);
        dataAccess.createAuthorization(authData);
        return authData;
    }
    public AuthData login(String username, String password) throws DataAccessException {
        UserData user = dataAccess.getUser(username);
        if(!user.password().equals(password) || user == null){
            throw new DataAccessException("Error: unauthorized");
        }
        String authToken = generateToken();
        AuthData authData = new AuthData(authToken, username);
        dataAccess.createAuthorization(authData);
        return authData;
    }
    public void logout(String authToken) throws DataAccessException{
        AuthData authData = dataAccess.getAuthorization(authToken);
        if(authData == null){
            throw new DataAccessException("Error: unauthorized");
        }
        dataAccess.deleteAuthorization(authToken);
    }
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}

