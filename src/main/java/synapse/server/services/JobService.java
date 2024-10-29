package synapse.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import synapse.server.models.Job;
import synapse.server.models.JobStatus;
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

    public String submitJob(
            String clientId,
            MultipartFile payload,
            MultipartFile data
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

        // Add details to the DB
        log("Job Saving (DB): " + jobId + ", " + clientId + ", " + JobStatus.INITIATED + ", Pending");
        jobRepository.save(job);

        return jobId;
    }

    public void updateJobStatus(String jobId, JobStatus status) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(status);
        jobRepository.save(job);
    }

    public String monitorJob(String jobId) {
        return "Job status: " + jobId;
    }

    // Additional methods for job lifecycle management
}