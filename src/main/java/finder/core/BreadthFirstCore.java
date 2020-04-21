package finder.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import finder.core.concurrent.Pause;
import finder.core.concurrent.PausePool;
import finder.model.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BreadthFirstCore implements Core {

	@Autowired
	private CrudRepository<Page, String> pageRepo;

	@JsonIgnore @Transient // TODO figure out why transient didn't "add" @Transient
	private transient final Pause pause = new Pause();

	private final Input input;
	private Set<Page> out;

	private WebClient webClient;

	private PausePool executor;
	private boolean stop;

	public BreadthFirstCore(Input input) {
		this.input = input;
	}

	@Override
	public void run() {
		webClient = WebClient.create();
		String domain = getDomain();
		var startingPage = new Page(input.getUrl(), domain, 0L);

		var visited = new HashSet<Page>();
		var queue = new LinkedList<Page>();
		queue.add(startingPage);
		out.add(startingPage);

		executor = new PausePool(input.getThreads(), pause);
		submitForRequest(startingPage);

		while (!queue.isEmpty() && !stop) {
			Page page = queue.poll();

			log.info("Now checking page with url {}. Will{} pause", page.getUrl(), (pause.isPaused() ? "" : " NOT"));
			pause.await();

			await(page); visited.add(page);

			handleNewUrls(visited, queue, page);

			page.find(input.getWhat());
		}

		out.forEach(p -> {
			if (p.getStatus().equals(Page.Status.QUEUED)) p.setStatus(Page.Status.CANCELLED);
		});
		executor.shutdownNow();
	}

	private void handleNewUrls(HashSet<Page> visited, LinkedList<Page> queue, Page page) {
		Set<Page> urls = page.findUrls();
		urls.removeAll(visited);

        var newPages = new HashSet<Page>();

		for (Page url : urls) {
			if (visited.size() + queue.size() + newPages.size() >= input.getMaxUrls())
				break;
            newPages.add(url);
		}

        queue.addAll(newPages);
        out.addAll(newPages);
		pageRepo.saveAll(newPages);

		newPages.forEach(this::submitForRequest);
		log.info("Submitted {} urls for request", urls.size());
	}

	private String getDomain() {
		try {
			return new URI(input.getUrl()).getHost();
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

	@Override
	public void pause() {
		pause.pause();
	}

	@Override
	public void stop() {
		stop = true;
	}

	@Override
	public void play() {
		pause.resume();
	}

}
