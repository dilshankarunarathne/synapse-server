package synapse.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import synapse.server.handlers.WebSocketHandler;
import synapse.server.models.JobStatus;
import synapse.server.models.JobType;
import synapse.server.services.AuthService;
import synapse.server.services.JobService;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static synapse.server.ServerApplication.log;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private AuthService authService;

    @Autowired
    private WebSocketHandler webSocketHandler;

    @PostMapping
    public ResponseEntity<String> createJob(
            @RequestHeader("Authorization") String token,
            @RequestParam("clientId") String clientId,
            @RequestParam("payload") MultipartFile payload,
            @RequestParam("data") MultipartFile data,
            @RequestParam("jobType") JobType jobType
    ) throws IOException, NoSuchAlgorithmException {
        if (!authService.verifyToken(token)) {
            log("Unauthorized request received for job creation");
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String jobId = jobService.submitJob(clientId, payload, data, jobType);
        webSocketHandler.sendMessageToAll("New job created: " + jobId);
        log("Job creation successful: " + jobId);
        return ResponseEntity.ok("Job created successfully with ID: " + jobId);
    }

    @PatchMapping("/{jobId}/status")
    public ResponseEntity<String> updateJobStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable String jobId,
            @RequestParam("status") JobStatus status
    ) {
        if (!authService.verifyToken(token)) {
            log("Unauthorized request received for updating job status");
            return ResponseEntity.status(401).body("Unauthorized");
        }
        jobService.updateJobStatus(jobId, status);
        log("Job status updated: " + jobId + " to " + status);
        return ResponseEntity.ok("Job status updated to: " + status);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<String> monitorJob(@RequestHeader("Authorization") String token, @PathVariable String jobId) {
        if (!authService.verifyToken(token)) {
            log("Unauthorized request received for query job ");
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String status = jobService.monitorJob(jobId);
        log("Job [" + jobId + "] status: " + status);
        return ResponseEntity.ok("Job [" + jobId + "] status: " + status);
    }
}