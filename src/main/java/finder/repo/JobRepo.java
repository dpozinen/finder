package finder.repo;

import finder.model.Job;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepo extends CrudRepository<Job, String> {}
