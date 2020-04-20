package finder.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
	@JsonIgnore
	private transient final Finder finder;
	private final Set<Page> pages = new HashSet<>();
	@JsonIgnore
	private transient final Pause pause = new Pause();
	private volatile Status status;

	public Job(Finder.Input input) {
		this.finder = new Finder(input, pause, pages);
		this.status = Status.QUEUED;
	}

	public void run() {
		finder.find();
		status = Status.DONE;
	}

	public void pause() {
		status = Status.PAUSED;
		pause.pause();
	}

	public void play() {
		status = Status.RUNNING;
		pause.resume();
	}

	public void stop() {
		finder.stop();
		status = Status.CANCELLED;
	}

	public void reset() {
		stop();
	}

	enum Status {
		DONE, PAUSED, RUNNING, QUEUED, CANCELLED
	}
}
