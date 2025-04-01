package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.*;
import java.net.*;
import java.util.*;


public class ServerFacade {
    private final String serverURL;
    private String authToken;
    private final Gson gson = new Gson();

    public ServerFacade(String serverURL) {
        this.serverURL = serverURL;
    }

    public ServerFacade(int port) {
        this("http://localhost:" + port + "/");
    }

    public Map<String, Object> register(String username, String password, String email) throws Exception {
        URL url = new URI(serverURL + "/user").toURL();
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.addRequestProperty("Content-Type", "application/json");
        String requestBody = gson.toJson(Map.of("username", username, "password", password, "email", email));
        httpURLConnection.getOutputStream().write(requestBody.getBytes());
        Map<String, Object> response = HttpUtils.parseResponse(httpURLConnection);
        if (response.containsKey("authToken")) {
            this.authToken = response.get("authToken").toString();
        }
        return response;
    }

    public Map<String, Object> login(String username, String password) throws Exception {

    }

    public void logout() throws Exception {

    }


    public Map<String, Object> createMyGame(String gameName) throws Exception {
    }

    public List<Map<String, Object>> listOfGames() throws Exception {
    }

    public void joinGame(String gameID, String playerColor) throws Exception {

    }
}
