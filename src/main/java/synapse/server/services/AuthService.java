package synapse.server.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {

    @Value("${auth.server.url}")
    private String authServerUrl;

    private final RestTemplate restTemplate;

    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean verifyToken(String token) {
        String url = authServerUrl + "/verify?token=" + token;
        Boolean isValid = restTemplate.getForObject(url, Boolean.class);
        return isValid != null && isValid;
    }
}
