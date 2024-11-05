package synapse.server.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import synapse.server.models.requests.CreateJobRequest;
import synapse.server.services.JobService;
import synapse.server.models.JobStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static synapse.server.ServerApplication.log;
import static synapse.server.handlers.MessageParser.*;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    @Lazy
    private JobService jobService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log("Client connected: " + session.getId());
        // TODO: update the clients list
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log("Received message: [" + session.getId() + "] " + message.getPayload());

        // store the message in a file
        CreateJobRequest jobRequest = parseResponse(message.getPayload());

        int nClients = extractNClients(jobRequest.getPayload());

        if (nClients == 1) {
            // TODO assign the job to a single worker
        } else {
            assignCollaborativeJob(jobRequest.getPayload(), jobRequest.getData(), nClients);
        }
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

    public void assignCollaborativeJob(String code, String data, int numClients) {
        synchronized (sessions) {
            String[] lines = data.split("\n");
            int linesPerClient = (int) Math.ceil((double) lines.length / numClients);
            int segment = 1;

            for (WebSocketSession session : sessions) {
                int start = (segment - 1) * linesPerClient;
                int end = Math.min(start + linesPerClient, lines.length);
                StringBuilder clientData = new StringBuilder();

                for (int i = start; i < end; i++) {
                    clientData.append(lines[i]).append("\n");
                }

                String finalClientData = clientData.toString();
                int finalSegment = segment;

                executorService.submit(() -> {
                    try {
                        session.sendMessage(new TextMessage("New job segment " + finalSegment + " assigned: " + code + "\n" + finalClientData));
                        log("Job segment [" + finalSegment + "] assigned to client: " + session.getId());
                        jobService.updateJobStatus(code, JobStatus.ASSIGNED);
                    } catch (IOException e) {
                        log("Error sending job segment to client: " + e.getMessage());
                    }
                });

                segment++;
            }
        }
    }
}