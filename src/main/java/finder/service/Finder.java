package finder.service;

import finder.model.Page;
import finder.repo.PageRepo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Finder {

	private final PageRepo repo;
	private final Input input;

	public Finder(Input input, PageRepo repo) {
		this.input = input;
		this.repo = repo;
	}

	public void find() {
		LinkedList<Page> queue = new LinkedList<>();
		String domain = getDomain();
		Page startingPage = new Page(input.url, domain, 0L);
		Set<Page> visited = new HashSet<>();

		queue.add(startingPage);
		repo.pages.add(startingPage);

		int parsed = 0;
		while (!queue.isEmpty() && parsed < input.maxUrls) {
			Page page = queue.poll();
			page.request();
			visited.add(page);

			Set<Page> urls = page.findUrls();
			urls.removeAll(visited);
			queue.addAll(urls);
			repo.pages.addAll(urls);

			page.find(input.what);
			parsed++;
		}
	}

	private String getDomain() {
		try {
			return new URI(input.url).getHost();
		} catch ( URISyntaxException e ) {
			return "";
		}
	}

	static class Input {
		private final String what;
		private final String url;
		private final short threads;
		private final short maxUrls;

		Input(String what, String url, short threads, short maxUrls) {
			this.what = what;
			this.url = url;
			this.threads = threads;
			this.maxUrls = maxUrls;
		}
	}
}
