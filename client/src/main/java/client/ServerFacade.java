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

    public ServerFacade(String serverURL) {
        this.serverURL = serverURL;
    }

    public ServerFacade(int port) {
        this("http://localhost:" + port + "/");
    }

    public Map<String, Object> register(String username, String password, String email) throws Exception {
        return postRequest("/user", Map.of("username", username, "password", password, "email", email), null);
    }

    public Map<String, Object> login(String username, String password) throws Exception {
        return postRequest("/session", Map.of("username", username, "password", password), null);
    }

    private Map<String, Object> postRequest(String serverUrl, Map<String, String> request, String authToken) throws Exception{
        URI uri = new URI(serverURL + serverUrl);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        if (authToken != null) {
            connection.setRequestProperty("Authorization", authToken);
        }
        writeRequest(connection, request);
        return response(connection);
    }

    private void writeRequest(HttpURLConnection connection, Map<String, String> request) throws Exception{
        connection.setDoOutput(true);
        connection.addRequestProperty("Content-Type", "application/json");
        try (var outputStream = connection.getOutputStream()) {
            String jsonBody = new Gson().toJson(request);
            outputStream.write(jsonBody.getBytes());
        }
    }

    private Map<String, Object> response(HttpURLConnection http) throws Exception{
        http.connect();
        int status = http.getResponseCode();
        if (status < 300 && status >= 200) {
            try (var in = http.getInputStream()) {
                Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
                Map<String, Object> response = new Gson().fromJson(new InputStreamReader(in), mapType);
                if (response.containsKey("authToken")) {
                    this.authToken = (String) response.get("authToken");
                }
                return response;
            }
        } else {
            try (var in = http.getErrorStream()) {
                throw new Exception(new Gson().fromJson(new InputStreamReader(in), Map.class).get("message").toString());
            }
        }
    }


}
