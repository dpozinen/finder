package finder.repo;

import finder.model.Page;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface PageRepo extends CrudRepository<Page, String> {

	default void updateStatus(Page page) {
		BiFunction<Page, Page, Page> merge = (p1, p2) -> p1.setStatus(p2.getStatus())
														   .setStatusCode(p2.getStatusCode())
														   .setErrorMsg(p2.getErrorMsg());
		findById(page.getId()).map(p -> merge.apply(p, page)).ifPresent(this::save);
	}

	default Set<Page> cancel(Collection<String> ids) {
		Set<Page> cancelled = StreamSupport.stream(findAllById(ids).spliterator(), false)
										   .filter(p -> p.getStatus().equals(Page.Status.QUEUED))
										   .map(p -> p.setStatus(Page.Status.CANCELLED))
										   .collect(Collectors.toSet());
		saveAll(cancelled);

		return cancelled;
	}

}
