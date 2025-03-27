package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Map;

public class HttpUtils {
    public static Map<String, Object> parseResponse(HttpURLConnection connection) throws Exception {
        int status = connection.getResponseCode();
        if (status < 300 && status >= 200 ) {
            try (var in = connection.getInputStream()) {
                if (in.available() == 0) {
                    return Map.of();
                }
                return new Gson().fromJson(new InputStreamReader(in), new TypeToken<Map<String, Object>>(){}.getType());
            }
        } else {
            try (var in = connection.getErrorStream()) {
                if (in == null) {
                    throw new Exception("Unknown error occurred");
                }
                String error = new Gson().fromJson(new InputStreamReader(in), Map.class).get("message").toString();
                throw new Exception(error);
            }
        }
    }
}