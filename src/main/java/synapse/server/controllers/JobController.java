package synapse.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import synapse.server.handlers.WebSocketHandler;
import synapse.server.services.AuthService;
import synapse.server.services.JobService;

import java.io.IOException;

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
            @RequestBody String jobDetails
    ) {
        if (!authService.verifyToken(token)) {
            log("Unauthorized request received for job creation with token: " + token);
            return ResponseEntity.status(401).body("Unauthorized");
        }
        jobService.createJob(jobDetails);
        webSocketHandler.sendMessageToAll("New job created: " + jobDetails);
        log("Job creation successful: " + jobDetails);
        return ResponseEntity.ok("Job created successfully");
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<String> monitorJob(@RequestHeader("Authorization") String token, @PathVariable String jobId) {
        if (!authService.verifyToken(token)) {
            log("Unauthorized request received for query job with token: " + token);
            return ResponseEntity.status(401).body("Unauthorized");
        }
        jobService.monitorJob(jobId);
        log("Job status retrieved successfully: " + jobId);
        return ResponseEntity.ok("Job status retrieved successfully");
    }
}