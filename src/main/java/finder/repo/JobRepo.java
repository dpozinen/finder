package finder.repo;

import finder.api.Job;
import org.springframework.data.repository.CrudRepository;

public interface JobRepo extends CrudRepository<Job<?>, String> {}
