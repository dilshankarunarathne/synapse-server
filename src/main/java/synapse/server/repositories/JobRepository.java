package synapse.server.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import synapse.server.models.Job;

public interface JobRepository extends MongoRepository<Job, String> {
}
