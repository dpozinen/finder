package finder.repo;

import finder.model.Page;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class PageRepo {

	public Set<Page> pages = new CopyOnWriteArraySet<>();

}
