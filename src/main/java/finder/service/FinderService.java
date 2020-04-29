package finder.service;

import finder.api.Job;
import finder.core.FinderInput;
import finder.core.Page;
import finder.repo.JobRepo;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FinderService {
	private final JobRepo jobRepo;
	private final ConcurrentHashMap<String, Job<Set<Page>>> jobs = new ConcurrentHashMap<>(8, 0.9f, 2);
	private final ExecutorService jobPool = Executors.newFixedThreadPool(4);

	public FinderService(JobRepo jobRepo) {
		this.jobRepo = jobRepo;
	}

	public boolean addJob(Map<String, String> form) {
		boolean valid = FinderInput.validate(form);
		if (valid) {
			int maxThreads = Integer.parseInt(form.get("maxThreads"));
			int maxUrls = Integer.parseInt(form.get("maxUrls"));
			String startingUrl = form.get("startingUrl");
			String searchStr = form.get("searchStr");

			var finderInput = new FinderInput(searchStr, startingUrl, maxUrls, maxThreads);
			var job = new Job<>(finderInput);
			jobRepo.save(job);
			jobs.put(job.getId(), job);
		}
		return false;
	}

	public boolean pause(String id) {
		var job = jobs.get(id);
		if (job != null) return job.pause();
		return false;
	}
}
