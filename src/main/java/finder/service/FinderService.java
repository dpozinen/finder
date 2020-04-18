package finder.service;

import finder.model.Page;
import finder.repo.PageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class FinderService {

	@Autowired
	private PageRepo repo;
	private Finder finder;

	public Set<Page> update() {
		return repo.pages;
	}

	public void find(Map<String, String> form) {
		var threads = form.get("threads");
		var url = form.get("url");
		var find = form.get("find");
		var maxUrls = form.get("maxUrls");

		Finder.Input input = new Finder.Input(find, url, Short.parseShort(threads), Short.parseShort(maxUrls));
		this.finder = new Finder(input, repo);
		finder.find();
	}

	public void pause() {
		finder.pause();
	}

	public void play() {
		finder.play();
	}

	public void stop() {
		finder.stop();
	}

	public void reset() {
		stop();
		finder.reset();
	}
}
