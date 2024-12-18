package synapse.server.handlers;

import org.json.JSONObject;
import synapse.server.models.requests.CreateJobRequest;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageParser {
    public static int[] parseResult(String response) {
        Pattern pattern = Pattern.compile("RESULT:(\\d+):(\\d+)");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            int jobId = Integer.parseInt(matcher.group(1));
            int result = Integer.parseInt(matcher.group(2));
            return new int[]{jobId, result};
        }
        throw new IllegalArgumentException("Invalid response format");
    }

    public static CreateJobRequest parseResponse(String jsonString) {
        // Parse the JSON string
        JSONObject jsonObject = new JSONObject(jsonString);

        // Extract fields
        String type = jsonObject.getString("type");
        String payloadBase64 = jsonObject.getString("payload");
        String dataBase64 = jsonObject.getString("data");

        // Decode Base64 encoded strings
        byte[] payloadBytes = Base64.getDecoder().decode(payloadBase64);
        byte[] dataBytes = Base64.getDecoder().decode(dataBase64);

        String payload = new String(payloadBytes);
        String data = new String(dataBytes);

        // Print the extracted parts
//        System.out.println("Type: " + type);
//        System.out.println("Payload: " + payload);
//        System.out.println("Data: " + data);

        return new CreateJobRequest(type, payload, data);
    }

    public static int extractNClients(String payload) {
        Pattern pattern = Pattern.compile("\\.n_clients=(\\d+)");
        Matcher matcher = pattern.matcher(payload);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new IllegalArgumentException("n_clients not found in payload");
    }
}