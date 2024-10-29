package synapse.server;

import logClient.LogClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

@SpringBootApplication
public class ServerApplication {
	public static LogClient logClient;

	public static void main(String[] args) {
		// Initialize the log client
        try {
            logClient = new LogClient();
        } catch (ProtocolException | MalformedURLException e) {
			System.out.println("Log server connection error: " + e.getMessage());
            throw new RuntimeException(e);
        }

        SpringApplication.run(ServerApplication.class, args);
	}

	public static void log(String message) throws IOException {
		logClient.log("[DServer] " + message);
	}

}
