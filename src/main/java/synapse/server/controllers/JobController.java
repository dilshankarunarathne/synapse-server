package synapse.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import synapse.server.services.AuthService;
import synapse.server.services.JobService;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<String> createJob(@RequestHeader("Authorization") String token, @RequestBody String jobDetails) {
        if (!authService.verifyToken(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        jobService.createJob(jobDetails);
        return ResponseEntity.ok("Job created successfully");
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<String> monitorJob(@RequestHeader("Authorization") String token, @PathVariable String jobId) {
        if (!authService.verifyToken(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        jobService.monitorJob(jobId);
        return ResponseEntity.ok("Job status retrieved successfully");
    }
}
