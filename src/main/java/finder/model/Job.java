package finder.model;

import finder.service.Finder;
import finder.service.Pause;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@RedisHash("Job")
public @Data class Job implements Serializable {

	@Id
	private String id;
	private transient final Finder finder;
	private final Set<Page> pages = new HashSet<>();
	private final Pause pause = new Pause();

	public Job(Finder.Input input) {
		this.finder = new Finder(input, pause, pages);
	}

	public void run() {
		finder.find();
	}

	public void pause() {
		pause.pause();
	}

	public void play() {
		pause.resume();
	}

	public void stop() {
		finder.stop();
	}

	public void reset() {
		stop();
	}

}
