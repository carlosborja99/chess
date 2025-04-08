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
    public String getServerURL(){
        return serverURL;
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
        URL url = new URI(serverURL + "/session").toURL();
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.addRequestProperty("Content-Type", "application/json");
        String requestBody = gson.toJson(Map.of("username", username, "password", password));
        httpURLConnection.getOutputStream().write(requestBody.getBytes());
        Map<String, Object> response = HttpUtils.parseResponse(httpURLConnection);
        if (response.containsKey("authToken")) {
            this.authToken = response.get("authToken").toString();
        }
        return response;
    }

    public void logout() throws Exception {
        URL url = new URI(serverURL + "/session").toURL();
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("DELETE");
        httpURLConnection.addRequestProperty("Authorization", authToken);
        HttpUtils.parseResponse(httpURLConnection);
        this.authToken = null;
    }


    public Map<String, Object> createMyGame(String gameName) throws Exception {
        URL url = new URI(serverURL + "/game").toURL();
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.addRequestProperty("Content-Type", "application/json");
        httpURLConnection.addRequestProperty("Authorization", authToken);
        String requestBody = gson.toJson(Map.of("gameName", gameName));
        httpURLConnection.getOutputStream().write(requestBody.getBytes());
        return HttpUtils.parseResponse(httpURLConnection);
    }

    public List<Map<String, Object>> listOfGames() throws Exception {
        URL url = new URI(serverURL + "/game").toURL();
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.addRequestProperty("Authorization", authToken);
        Map<String, Object> response = HttpUtils.parseResponse(httpURLConnection);
        return (List<Map<String, Object>>) response.get("games");
    }

    public Map<String, Object> joinGame(String gameID, String playerColor) throws Exception {
        URL url = new URI(serverURL + "/game").toURL();
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("PUT");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.addRequestProperty("Content-Type", "application/json");
        httpURLConnection.addRequestProperty("Authorization", authToken);
        String requestBody = gson.toJson(Map.of("gameID", gameID, "playerColor", playerColor));
        httpURLConnection.getOutputStream().write(requestBody.getBytes());
        return HttpUtils.parseResponse(httpURLConnection);
    }

    public String getAuthToken(){
        return  authToken;
    }

    public String getHost() {
        return null;
    }
}
