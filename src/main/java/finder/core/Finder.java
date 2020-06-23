package finder.core;

import finder.api.Core;
import finder.api.Job;
import finder.api.concurrent.Pause;
import finder.api.concurrent.PausePool;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Component @Scope("prototype")
public abstract class Finder implements Core<Set<Page>> {
	protected final WebClient webClient = WebClient.create();

	private final finder.redis.Publisher redisPublisher;
	private final LinkedList<Page> queue = new LinkedList<>();
	private final Set<Page> visited = new HashSet<>();

	private final Pause pause = new Pause();
	private PausePool requestPool;
	private boolean stop;

	private final Input input;

	public Finder(Input input, finder.redis.Publisher redisPublisher) {
		this.input = input;
		this.requestPool = new PausePool(input.maxThreads, pause);
		this.redisPublisher = redisPublisher;
	}

	protected abstract void find();

	@Override public void run() {
		if (requestPool.isShutdown()) this.requestPool = new PausePool(input.maxThreads, pause);
		Page page = new Page(input.startingUrl);
		queue.add(page);
		spreadMessage(List.of(page));
		requestPool.submit(() -> page.request(webClient));
		find();
	}

	@Override public void pause() {
		pause.pause();
	}

	@Override public void cancel() {
		stop();
		queue.clear();
	}

	@Override public Set<Page> results() {
		return visited;
	}

	@Override public void stop() {
		this.stop = true;
		requestPool.shutdownNow();
	}

	protected Page next() {
		return queue.poll();
	}

	protected boolean proceed() {
		return !stop && !queue.isEmpty();
	}

	protected void waitIfPaused() {
		pause.await();
	}

	protected void spreadMessage(Collection<Page> pages) {
//		redisPublisher.publishAdd(jobId, pages);
	}

	protected void append(Collection<Page> following) {
		queue.addAll(following);
	}

	protected void prepend(Collection<Page> following) {
		queue.addAll(0, following);
	}

	public static record Input(String search,
							   String startingUrl,
							   int maxUrls,
							   int maxThreads,
							   finder.redis.Publisher messagePublisher)
			implements Job.Input<Set<Page>> {

		public static boolean validate(Map<String, String> form) {
			String maxThreads = form.get("maxThreads");
			String maxUrls = form.get("maxUrls");
			String startingUrl = form.get("startingUrl");
			String searchStr = form.get("search");

			return true;
		}

		@Override public Core<Set<Page>> core() { // add dfs
			return new BreadthFirstFinder(this, messagePublisher);
		}

	}

}
