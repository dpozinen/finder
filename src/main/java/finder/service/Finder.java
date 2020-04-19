package finder.service;

import finder.model.Page;
import finder.model.Status;
import lombok.Setter;
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

	private final Input input;
	private final Set<Page> out;

	private WebClient webClient;

	private PausePool executor;
	private final Pause pause;
	private volatile boolean stop;

	public Finder(Input input, Pause pause, Set<Page> out) {
		this.input = input;
		this.pause = pause;
		this.out = out;
	}

	public void find() {
		webClient = WebClient.create();
		String domain = getDomain();
		var startingPage = new Page(input.url, domain, 0L);

		var visited = new HashSet<Page>();
		var queue = new LinkedList<Page>();
		queue.add(startingPage);
		out.add(startingPage);

		executor = new PausePool(input.threads, pause);
		submitForRequest(startingPage);

		while (!queue.isEmpty() && !stop) {
			Page page = queue.poll();

			log.info("Now checking page with url {}. Will{} pause", page.getUrl(), (pause.isPaused() ? "" : " NOT"));
			pause.await();

			await(page); visited.add(page);

			handleNewUrls(visited, queue, page);

			page.find(input.what);
		}

		out.forEach(p -> {
			if (p.getStatus().equals(Status.QUEUED)) p.setStatus(Status.CANCELLED);
		});
		executor.shutdownNow();
	}

	private void handleNewUrls(HashSet<Page> visited, LinkedList<Page> queue, Page page) {
		Set<Page> urls = page.findUrls();
		urls.removeAll(visited);

        var newPages = new HashSet<Page>();

		for (Page url : urls) {
			if (visited.size() + queue.size() + newPages.size() >= input.maxUrls)
				break;
            newPages.add(url);
		}

        queue.addAll(newPages);
        out.addAll(newPages);

		newPages.forEach(this::submitForRequest);
		log.info("Submitted {} urls for request", urls.size());
	}

	private String getDomain() {
		try {
			return new URI(input.url).getHost();
		} catch (URISyntaxException e) {
			return "";
		}
	}

	private void await(Page page) {
		try {
			page.getResponseReceivedSignal().await(5L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void submitForRequest(Page page) {
		page.setResponseReceivedSignal(new CountDownLatch(1));
		executor.execute(() -> {
			String url = page.getUrl();
			log.info("Requesting %s".formatted(url));
			page.request(webClient);
			log.info("Got response from %s".formatted(url));
		});
	}

	public void stop() {
		stop = true;
	}

	public static class Input {
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
