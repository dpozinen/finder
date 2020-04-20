package finder.service;

import finder.model.Job;
import finder.model.Page;
import finder.repo.JobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class FinderService {

	@Autowired
	private JobRepo repo;
	private final ExecutorService pool = Executors.newFixedThreadPool(3);
	private final Map<String, Future<?>> futures = new HashMap<>();

	public Set<Page> update(String id) {
		return repo.findById(id).map(Job::getPages).orElseGet(Set::of);
	}

	public String find(Map<String, String> form) {
		var threads = form.get("threads");
		var url = form.get("url");
		var find = form.get("find");
		var maxUrls = form.get("maxUrls");

		Finder.Input input = new Finder.Input(find, url, Short.parseShort(threads), Short.parseShort(maxUrls));
		Job job = new Job(input);

		repo.save(job);
		var jobId = job.getId();

		Future<?> future = pool.submit(job::run);
		futures.put(jobId, future);

		return jobId;
	}

	public void pause(String id) {
		repo.findById(id).ifPresent(Job::pause);
	}

	public void play(String id) {
		repo.findById(id).ifPresent(Job::play);
	}

	public void stop(String id) {
		repo.findById(id).ifPresent(j -> {
			j.stop();
			futures.get(id).cancel(true);
		});
	}

	public void reset(String id) {
		repo.findById(id).ifPresent(j -> { j.stop(); j.reset(); });
	}

	public Optional<Job> getJob(String id) {
		return repo.findById(id);
	}
}
