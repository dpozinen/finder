package finder.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import finder.core.concurrent.Pause;
import finder.core.concurrent.PausePool;
import finder.model.Page;
import finder.repo.PageRepo;
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
import java.util.stream.Collectors;

@Slf4j
public abstract class Finder implements Core {

	@Autowired private RedisMessagePublisher redisPublisher;
	@Autowired private RedisTemplate<String, Object> redisTemplate;
	@Autowired private PageRepo pageRepo;

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

	protected void addNewPage(Page page) {
		pageRepo.save(page);
		redisTemplate.opsForSet().add(jobId, page.getId());
		redisPublisher.publishAdd(jobId, page);
	}

	protected void addNewPages(Collection<Page> pages) {
		pageRepo.saveAll(pages);
		var ids = pages.stream().map(Page::getId).toArray(String[]::new);
		redisTemplate.opsForSet().add(jobId, ids);
		redisPublisher.publishAdd(jobId, pages);
	}

	protected void cancelQueued() {
		Set<String> pages = redisTemplate.opsForSet().members(jobId).stream()
										 .map(p -> (String)p).collect(Collectors.toSet());

		Set<Page> cancelled = pageRepo.cancel(pages);

		redisPublisher.publishUpdate(jobId, cancelled);
	}

	protected void updateStatus(Page page) {
		pageRepo.updateStatus(page);
		redisPublisher.publishUpdate(jobId, List.of(page));
	}
}
