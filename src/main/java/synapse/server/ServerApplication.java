package synapse.server;

import logClient.LogClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.MalformedURLException;
import java.net.ProtocolException;

@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
        try {
            LogClient logClient = new LogClient();
        } catch (ProtocolException | MalformedURLException e) {
			System.out.println("Log server connection error: " + e.getMessage());
            throw new RuntimeException(e);
        }
        SpringApplication.run(ServerApplication.class, args);
	}

}
