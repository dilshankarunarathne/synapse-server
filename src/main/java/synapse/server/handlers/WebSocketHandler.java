package synapse.server.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import synapse.server.models.JobStatus;
import synapse.server.services.JobService;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static synapse.server.ServerApplication.log;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    private JobService jobService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log("Client connected: " + session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log("Received message: [" + session.getId() + "] " + message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log("Client disconnected: " + session.getId());
    }

    public void sendMessageToAll(String message) {
        synchronized (sessions) {
            for (WebSocketSession session : sessions) {
                try {
                    session.sendMessage(new TextMessage(message));
                    log("Message sent to client: " + session.getId());
                } catch (Exception e) {
                    log(e.getMessage());
                }
            }
        }
    }

    public void distributeJob(String jobId) {
        synchronized (sessions) {
            for (WebSocketSession session : sessions) {
                executorService.submit(() -> {
                    try {
                        session.sendMessage(new TextMessage("New job assigned: " + jobId));
                        log("Job [" + jobId + "] assigned: " + jobId + " to " + session.getId());
                        jobService.updateJobStatus(jobId, JobStatus.ASSIGNED);
                    } catch (IOException e) {
                        log("Error sending job to client: " + e.getMessage());
                    }
                });
            }
        }
    }

    public void assignSingleWorkerJob(String jobId) {
        synchronized (sessions) {
            if (!sessions.isEmpty()) {
                WebSocketSession session = sessions.iterator().next();
                executorService.submit(() -> {
                    try {
                        session.sendMessage(new TextMessage("New job assigned: " + jobId));
                        log("Job [" + jobId + "] assigned: " + jobId + " to " + session.getId());
                        jobService.updateJobStatus(jobId, JobStatus.ASSIGNED);
                    } catch (IOException e) {
                        log("Error sending job to client: " + e.getMessage());
                    }
                });
            }
        }
    }

    public void assignCollaborativeJob(String jobId) {
        synchronized (sessions) {
            int segment = 1;
            for (WebSocketSession session : sessions) {
                int finalSegment = segment;
                executorService.submit(() -> {
                    try {
                        session.sendMessage(new TextMessage("New job segment " + finalSegment + " assigned: " + jobId));
                        log("Job [" + jobId + ":" + finalSegment + "] assigned: " + jobId + " to " + session.getId());
                        jobService.updateJobStatus(jobId, JobStatus.ASSIGNED);
                    } catch (IOException e) {
                        log("Error sending job to client: " + e.getMessage());
                    }
                });
                segment++;
            }
        }
    }
}