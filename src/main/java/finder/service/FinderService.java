package finder.service;

import finder.model.Job;
import finder.model.Page;
import finder.repo.JobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class FinderService {

	@Autowired
	private JobRepo repo;

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
		job.run();
		return job.getId();
	}

	public void pause(String id) {
		repo.findById(id).ifPresent(Job::pause);
	}

	public void play(String id) {
		repo.findById(id).ifPresent(Job::play);
	}

	public void stop(String id) {
		repo.findById(id).ifPresent(Job::stop);
	}

	public void reset(String id) {
		repo.findById(id).ifPresent(j -> { j.stop(); j.reset(); });
	}
}
