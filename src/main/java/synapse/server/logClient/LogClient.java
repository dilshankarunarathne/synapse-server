package synapse.server.logClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
public class LogClient {

    @Value("${log.server.url}")
    private String logServerUrl;

    private static final int MAX_LOG_LENGTH = 100;

    @PostConstruct
    public void init() {
        System.out.println("Initializing log client... " + logServerUrl);
    }

    public void log(String message) throws IOException {
        message = truncateMessage(message);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String jsonBody = "{ \"message\": \"" + message + "\" }";
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(logServerUrl);

        System.out.println("Sending log message to log server: " + message);
        HttpEntity<String> response = null;
        try {
            response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.POST,
                    entity,
                    String.class);
        } catch (Exception e) {
            System.out.println("Error sending log message: " + e.getMessage());
        }
        System.out.println(response);
    }

    private String truncateMessage(String message) {
        if (message.length() > MAX_LOG_LENGTH) {
            return message.substring(0, MAX_LOG_LENGTH) + "...";
        }
        return message;
    }
}
