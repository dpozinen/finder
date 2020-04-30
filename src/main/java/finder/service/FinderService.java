package finder.service;

import finder.api.Job;
import finder.core.Finder;
import finder.core.Page;
import finder.repo.JobRepo;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class FinderService {
	private final JobRepo jobRepo;
	private final ConcurrentHashMap<String, Job<Set<Page>>> jobs = new ConcurrentHashMap<>(8, 0.9f, 2);
	private final ConcurrentHashMap<String, Future<?>> jobFutures = new ConcurrentHashMap<>(8, 0.9f, 2);
	private final ExecutorService jobPool = Executors.newFixedThreadPool(4);

	public FinderService(JobRepo jobRepo) {
		this.jobRepo = jobRepo;
	}

	public Optional<Job.State> state(String id) {
		var job = jobs.get(id);
		if (job != null)
			return Optional.of(job.getState());
		else
			return jobRepo.findById(id).map(Job::getState);
	}

	public Optional<Set<Page>> results(String id) {
		var job = jobs.get(id);
		if (job != null)
			return job.results();
		else {
			Optional<Job<Set<Page>>> doneJob = jobRepo.findById(id);
			if (doneJob.isPresent()) return doneJob.get().results();
			else return Optional.empty();
		}
	}

	public String addJob(Map<String, String> form) {
		boolean valid = Finder.Input.validate(form);
		if (valid) {
			int maxThreads = Integer.parseInt(form.get("maxThreads"));
			int maxUrls = Integer.parseInt(form.get("maxUrls"));
			String startingUrl = form.get("startingUrl");
			String searchStr = form.get("search");

			var finderInput = new Finder.Input(searchStr, startingUrl, maxUrls, maxThreads);
			var job = new Job<>(finderInput);
			jobRepo.save(job);
			jobs.put(job.getId(), job);
			return job.getId();
		}
		return "";
	}

	public boolean play(String id) {
		var job = jobs.get(id);
		if (job != null) {
			Future<?> future = jobPool.submit(() -> { job.run(); jobs.remove(job.getId()); });
			jobFutures.put(job.getId(), future);
			return true;
		}
		return false;
	}

	public boolean stop(String id) {
		var job = jobs.get(id);
		if (job != null && job.stop()) {
			jobFutures.get(job.getId()).cancel(true);
			return true;
		}
		return false;
	}

	public boolean cancel(String id) {
		var job = jobs.get(id);
		if (job != null) return job.cancel();
		return false;
	}

	public boolean pause(String id) {
		var job = jobs.get(id);
		if (job != null) return job.pause();
		return false;
	}
}
