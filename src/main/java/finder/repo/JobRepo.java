package finder.repo;

import finder.api.Job;
import finder.core.Page;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface JobRepo extends CrudRepository<Job<Set<Page>>, String> {}
