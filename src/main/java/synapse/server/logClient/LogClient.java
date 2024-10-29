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

    @PostConstruct
    public void init() {
        System.out.println("Initializing log client... " + logServerUrl);
    }

    public void log(String message) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String jsonBody = "{ \"message\": \"" + message + "\" }";
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(logServerUrl);

        HttpEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.POST,
                entity,
                String.class);

        System.out.println(response);
    }
}