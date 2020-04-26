package finder.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import finder.core.concurrent.Pause;
import finder.core.concurrent.PausePool;
import finder.model.Page;
import finder.service.RedisMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class Finder implements Core {

	@Autowired RedisMessagePublisher redisPublisher;
	@Autowired RedisTemplate<String, Object> redisTemplate;
	protected Set<Page> out = new HashSet<>();

	@JsonIgnore @Transient // TODO figure out why transient didn't "add" @Transient
	protected transient final Pause pause = new Pause();

	protected final Input input;
	protected WebClient webClient;
	protected PausePool executor;
	protected boolean stop;
	protected final String jobId;

	protected Finder(Input input, String jobId) {
		this.input = input;
		this.jobId = jobId;
		this.executor = new PausePool(input.getThreads(), pause);
		this.webClient = WebClient.create();
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

	protected void submitForRequest(Page page) {
		page.setResponseReceivedSignal(new CountDownLatch(1));
		executor.execute(() -> {
			String url = page.getUrl();
			log.info("Requesting %s".formatted(url));
			page.request(webClient);
			log.info("Got response from %s".formatted(url));
		});
	}

	protected void addNewPage(Page page) {
		redisTemplate.opsForSet().add(jobId, page);
		redisPublisher.publishAdd(jobId, page);
	}

	protected void addNewPages(Collection<Page> pages) {
		redisTemplate.opsForSet().add(jobId, pages.toArray(new Page[0]));
		redisPublisher.publishAdd(jobId, pages);
	}

	protected String getDomain() {
		try {
			return new URI(input.getUrl()).getHost();
		} catch (URISyntaxException e) { return ""; }
	}

	protected void await(Page page) {
		try {
			page.getResponseReceivedSignal().await(5L, TimeUnit.SECONDS);
		} catch (InterruptedException e) { e.printStackTrace(); }
	}

	protected void cancelQueued() {
		Set<Object> members = redisTemplate.opsForSet().members(jobId);
		redisTemplate.delete(jobId);
		log.info("Cancelling {} pages", members.size());
		Objects.requireNonNull(members).forEach(this::cancel);
		redisTemplate.opsForSet().add(jobId, members);
		redisPublisher.publishRefresh(jobId, members);
	}

	private void cancel(Object p) {
		var page = (Map<Object, Object>) p;
		if (page.get("status").equals(Page.Status.QUEUED.name())) page.put("status", Page.Status.CANCELLED);
	}
}
