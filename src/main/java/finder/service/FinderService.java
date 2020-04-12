package finder.service;

import finder.model.Page;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class FinderService {

	public Set<Page> update() {
		return Set.of(new Page(1L, "32", 1L, 3));
	}

	public boolean find(Map<String, String> form) {
		var threads = form.get("threads");
		var url = form.get("url");
		var find = form.get("find");

		if ( threads == null || url == null || find == null || !threads.matches("\\d+")) {
			return false;
		} else {
			Finder.Input input = new Finder.Input(find, url, Short.parseShort(threads));
			return new Finder(input).find();
		}
	}

}
