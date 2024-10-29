package logClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Value;

public class LogClient {

    @Value("${log.server.url}")
    private String logServerUrl;

    public LogClient() throws ProtocolException, MalformedURLException {
        URL url = new URL(new LogClient().logServerUrl);

        System.out.println("Using log server URL: " + url);
        
        // Create a connection
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            System.out.println("Error creating connection with the log server");
            throw new RuntimeException(e);
        }

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
    }

    public static void main(String[] args) {
        try {
            // JSON payload
            String jsonInputString = "{\"message\": \"test log 1\"}";

            // Write the JSON payload to the output stream
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get the response code
            int responseCode = conn.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);

            // Close the connection
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
