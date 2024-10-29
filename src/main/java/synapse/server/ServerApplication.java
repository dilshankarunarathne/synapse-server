package synapse.server;

import synapse.server.logClient.LogClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class ServerApplication {
    public static LogClient logClient;
    private static ExecutorService executorService;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ServerApplication.class, args);
        logClient = context.getBean(LogClient.class);
        executorService = context.getBean(ExecutorService.class);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log("Shutting down executor service...");
                executorService.shutdown();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                log("Error shutting down executor service: " + e.getMessage());
            }
        }));

        log("Distribution Server started and connected with the log server");
    }

    public static void log(String message) {
        try {
            logClient.log("[DServer] " + message);
        } catch (IOException e) {
            System.out.println("Error logging message: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}