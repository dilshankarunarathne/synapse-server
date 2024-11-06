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

    private int[] resultSegments = new int[2]; // TODO hardcoded for now
    private int segmentsRecieved = 0;
    private String jobLeader = null;
    String payload = null;

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

        if (message.getPayload().contains("RESULT")) {
            int[] result = parseResult(message.getPayload());
            log("Job [" + result[0] + "] completed with result: " + result[1]);

            resultSegments[segmentsRecieved] = result[0];
            segmentsRecieved++;

            if (segmentsRecieved == 2) {
                System.out.println("----------- All segments received -------------");
                // TODO: accumulate and send back to creator
                String request = buildLeaderRequest();
                sendToLeader(jobLeader, request);
            }

            return;
        } else {
            // store the message in a file
            CreateJobRequest jobRequest = parseResponse(message.getPayload());
            // Assign the ID of the job request received client
            jobRequest.setClientID(session.getId());

            int nClients = extractNClients(jobRequest.getPayload());

            if (nClients == 1) {
                // TODO assign the job to a single worker
            } else {
                assignCollaborativeJob(jobRequest, 2); // TODO hard coded for now
            }
        }
    }

    private String getFinalClientData() {
        String message =  Integer.toString(resultSegments[0]);
        message = message + "\n" + resultSegments[1];
        return message;
    }

    private String buildLeaderRequest() {
        return "LEAD|SEP|"
                + "DATA:" + getFinalClientData() + "|SEP|"
                + "PAYLOAD:" + payload + "|SEP|"
                + "END";
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

    public void assignCollaborativeJob(CreateJobRequest jobRequest, int numClients) {
        synchronized (sessions) {
            String[] lines = jobRequest.getData().split("\n");
            int linesPerClient = (int) Math.ceil((double) lines.length / numClients);
            int segment = 1;
            boolean jobLeaderAssigned = false;
            String jobLeaderID = null;

            for (WebSocketSession session : sessions) {
                if (session.getId().equals(jobRequest.getClientID())) {
                    continue; // Skip the client who submitted the job
                }

                if (!jobLeaderAssigned) {
                    jobLeaderID = session.getId();
                    jobRequest.setJobLeaderID(jobLeaderID);
                    jobLeaderAssigned = true;

                    // TODO send the leader the job info for accumulation
//                    assignJobLeader(jobRequest);
                    jobLeader = jobLeaderID;

                    continue; // Skip the job leader
                }

                int start = (segment - 1) * linesPerClient;
                int end = Math.min(start + linesPerClient, lines.length);
                StringBuilder clientData = new StringBuilder();

                for (int i = start; i < end; i++) {
                    clientData.append(lines[i]).append("\n");
                }

                String finalClientData = clientData.toString();
                int finalSegment = segment;

                payload = jobRequest.getPayload();

                executorService.submit(() -> {
                    try {
                        session.sendMessage(
                                new TextMessage(
                                        "JOB:" + finalSegment + "|SEP|"
                                                + "CREATOR:" + jobRequest.getClientID() + "|SEP|"
                                                + "LEADER:" + jobRequest.getJobLeaderID() + "|SEP|"
                                                + "DATA:" + finalClientData + "|SEP|"
                                                + "PAYLOAD:" + jobRequest.getPayload() + "|SEP|"
                                                + "END"
                                ));
                        System.out.println("-----------" + finalSegment + "-----------");
                        System.out.println("JOB:" + finalSegment + "|SEP|"
                                + "CREATOR:" + jobRequest.getClientID() + "|SEP|"
                                + "LEADER:" + jobRequest.getJobLeaderID() + "|SEP|"
                                + "DATA:" + finalClientData + "|SEP|"
                                + "PAYLOAD:" + jobRequest.getPayload() + "|SEP|"
                                + "END");
                        System.out.println("-----------" + finalSegment + "-----------");
                        log("Job segment [" + finalSegment + "] assigned to client: " + session.getId());
//                        jobService.updateJobStatus(jobRequest.getType(), JobStatus.ASSIGNED);
                    } catch (IOException e) {
                        log("Error sending job segment to client: " + e.getMessage());
                    }
                });

                segment++;
            }
        }
    }

    private void sendToLeader(String leaderID, String message) {
        System.out.println("--------- sending to leader ---------" + leaderID);
        synchronized (sessions) {
            for (WebSocketSession session : sessions) {
                System.out.print(session.getId() + " ");
                System.out.println("expected leader id: " + leaderID);
                if (session.getId().equals(leaderID)) {
                    System.out.println("leader thread found...");
                    executorService.submit(() -> {
                        try {
                            session.sendMessage(
                                    new TextMessage(message));
                            System.out.println("----------------------");
                            System.out.println(message);
                            System.out.println("----------------------");
                            log("Job leader assigned: " + session.getId());
                        } catch (IOException e) {
                            log("Error sending job leader assignment to client: " + e.getMessage());
                        }
                    });
                }
            }
        }
    }

//    private void assignJobLeader(CreateJobRequest jobRequest) {
//        synchronized (sessions) {
//            for (WebSocketSession session : sessions) {
//                if (session.getId().equals(jobRequest.getJobLeaderID())) {
//                    executorService.submit(() -> {
//                        try {
//                            session.sendMessage(
//                                    new TextMessage(
//                                            "LEAD\n" + jobRequest.getPayload()
//                            ));
//
//                            log("Job leader assigned: " + session.getId());
//                        } catch (IOException e) {
//                            log("Error sending job leader assignment to client: " + e.getMessage());
//                        }
//                    });
//                }
//            }
//        }
//    }
}