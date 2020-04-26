package finder.repo;

import finder.model.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Pair;

import java.util.Set;

public interface PageRepo extends CrudRepository<Pair<String, Set<Page>>, String> {}
