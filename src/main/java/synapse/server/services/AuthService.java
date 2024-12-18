package synapse.server.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.json.JSONObject;
import synapse.server.models.Job;
import synapse.server.models.JobStatus;

import static synapse.server.ServerApplication.log;

@Service
public class AuthService {

    @Value("${auth.server.url}")
    private String authServerUrl;

    public AuthService() {
    }

    public boolean verifyToken(String token) {
        log("Verifying token received from the client...");
        token = token.replace("Bearer ", "");

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(authServerUrl + "/verify-token");

        try {
            HttpEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.POST,
                    entity,
                    String.class);

            JSONObject jsonResponse = new JSONObject(response.getBody());
            String clientId = jsonResponse.getString("client_id");
            log("Client ID: " + clientId);

            System.out.println(response);

            return true;
        } catch (Exception e) {
            log("Error verifying token: " + e.getMessage());
            return false;
        }
    }
}
