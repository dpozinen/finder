package finder.service;

import finder.model.Page;
import finder.model.Status;
import finder.repo.PageRepo;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Finder {

	private final PageRepo repo;
	private final Input input;
	private ThreadPoolExecutor executor;

	private WebClient webClient;

	public Finder(Input input, PageRepo repo) {
		this.input = input;
		this.repo = repo;
	}

	public void find() {
		LinkedList<Page> queue = new LinkedList<>();
		String domain = getDomain();
		Page startingPage = new Page(input.url, domain, 0L);
		Set<Page> visited = new HashSet<>();

		webClient = WebClient.create();

		queue.add(startingPage);
		repo.pages.add(startingPage);

		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(input.threads);
		submitForRequest(startingPage);

		int parsed = 0;
		while (!queue.isEmpty() && parsed < input.maxUrls) {
			Page page = queue.poll();
			awaitResponse(page);
			visited.add(page);

			Set<Page> urls = page.findUrls();
			urls.removeAll(visited);
			queue.addAll(urls);
			repo.pages.addAll(urls);

			urls.forEach(this::submitForRequest);

			page.find(input.what);
			parsed++;
		}
		repo.pages.forEach(p -> {
			if (p.getStatus().equals(Status.QUEUED)) p.setStatus(Status.CANCELLED);
		});
		executor.shutdown();
	}

	private String getDomain() {
		try {
			return new URI(input.url).getHost();
		} catch ( URISyntaxException e ) {
			return "";
		}
	}

	private void awaitResponse(Page page) {
		try {
			page.getResponseReceivedSignal().await(20, TimeUnit.SECONDS);
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
	}

	private void submitForRequest(Page page) {
		page.setResponseReceivedSignal(new CountDownLatch(1));
		executor.execute(() -> page.request(webClient));
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
