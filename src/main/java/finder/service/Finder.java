package finder.service;

import finder.model.Page;
import finder.model.Status;
import finder.repo.PageRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Finder {

	private final PageRepo repo;
	private final Input input;

	private WebClient webClient;

	private PausePool executor;
	private Pause pause;
	private volatile boolean stop;

	public Finder(Input input, PageRepo repo) {
		this.input = input;
		this.repo = repo;
	}

	public void find() {
		webClient = WebClient.create();
		String domain = getDomain();
		var startingPage = new Page(input.url, domain, 0L);

		var visited = new HashSet<Page>();
		var queue = new LinkedList<Page>();
		queue.add(startingPage);
		repo.pages.add(startingPage);

		pause = new Pause();
		executor = new PausePool(input.threads, pause);
		submitForRequest(startingPage);

		int parsed = 0;
		while (!queue.isEmpty() && parsed < input.maxUrls && !stop) {
			Page page = queue.poll();

			log.info("Now checking page with url {}. Will {} pause", page.getUrl(), (pause.isPaused() ? "NOT" : ""));
			if (pause.isPaused()) pause.await();

			await(page);
			visited.add(page);

			handleNewUrls(visited, queue, page);

			page.find(input.what);
			parsed++;
		}

		repo.pages.forEach(p -> {
			if (p.getStatus().equals(Status.QUEUED)) p.setStatus(Status.CANCELLED);
		});
		executor.shutdown();
	}

	private void handleNewUrls(HashSet<Page> visited, LinkedList<Page> queue, Page page) {
		Set<Page> urls = page.findUrls();
		urls.removeAll(visited);
		queue.addAll(urls);
		repo.pages.addAll(urls);

		urls.forEach(this::submitForRequest);
		log.info("Submitted {} urls for request", urls.size());
	}

	private String getDomain() {
		try {
			return new URI(input.url).getHost();
		} catch ( URISyntaxException e ) {
			return "";
		}
	}

	private void await(Page page) {
		try {
			page.getResponseReceivedSignal().await(20L, TimeUnit.SECONDS);
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
	}

	private void submitForRequest(Page page) {
		page.setResponseReceivedSignal(new CountDownLatch(1));
		executor.execute(() -> page.request(webClient));
	}

	void pause() {
		log.info("Pausing finder");
		pause.pause();
	}

	void play() {
		log.info("Resuming finder");
		pause.resume();
	}

	void stop() {
		log.info("Stopping finder");
		stop = true;
	}

	void reset() {
		log.info("Resetting finder");
		stop();
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
