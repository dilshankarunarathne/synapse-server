package synapse.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import synapse.server.handlers.WebSocketHandler;
import synapse.server.models.Job;
import synapse.server.models.JobStatus;
import synapse.server.models.JobType;
import synapse.server.repositories.JobRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static synapse.server.ServerApplication.log;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    @Lazy
    private WebSocketHandler webSocketHandler;

    public String submitJob(
            String clientId,
            MultipartFile payload,
            MultipartFile data,
            JobType jobType
    ) throws IOException {
        // Generate a unique job ID
        String jobId = UUID.randomUUID().toString();

        // Save the payload and data files
        Path payloadPath = Paths.get("uploads/" + jobId + "_payload");
        Path dataPath = Paths.get("uploads/" + jobId + "_data");

        Files.createDirectories(payloadPath.getParent());
        Files.write(payloadPath, payload.getBytes());
        Files.write(dataPath, data.getBytes());

        // Create a new Job object and save it to MongoDB
        Job job = new Job();
        job.setJobId(jobId);
        job.setClientId(clientId);
        job.setStatus(JobStatus.INITIATED);
        job.setResult("Pending"); // Set initial result
        job.setPayloadPath(payloadPath.toString());
        job.setDataPath(dataPath.toString());
        job.setJobType(jobType); // Set job type

        // Add details to the DB
        log("Job Saving (DB): " + jobId + ", " + clientId + ", " + JobStatus.INITIATED + ", Pending, " + jobType);
        jobRepository.save(job);

        // Distribute job based on job type
        switch (jobType) {
            case SINGLE_WORKER:
                webSocketHandler.assignSingleWorkerJob(jobId);
                break;
            case DISTRIBUTIVE:
                webSocketHandler.distributeJob(jobId);
                break;
            case COLLABORATIVE:
                webSocketHandler.assignCollaborativeJob(jobId);
                break;
        }

        return jobId;
    }

    public void updateJobStatus(String jobId, JobStatus status) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(status);
        log("Job [" + jobId + "] status updated: " + status);
        jobRepository.save(job);
    }

    public void updateJobResult(String jobId, String result) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        job.setResult(result);
        job.setStatus(JobStatus.FINISHED);
        log("Job [" + jobId + "] result updated: " + result);
        jobRepository.save(job);
    }

    public String monitorJob(String jobId) {
        return "Job status: " + jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found")).getStatus();
    }

    // Additional methods for job lifecycle management
}