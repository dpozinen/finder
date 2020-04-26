package finder.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import finder.core.Core;
import finder.core.Input;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.annotation.Transient;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

@RedisHash("Job")
public @Data class Job implements Serializable {

	@Autowired
	private BiFunction<Input, String, ? extends Core> coreFactory;

	@Id
	private String id;
	@JsonIgnore	@Transient
	private transient Core core;

	private volatile Status status;
	private Input input;

	public Job() {}

	public Job(Input input) {
		this.input = input;
		this.status = Status.QUEUED;
	}

	public void run() {
		core = coreFactory.apply(input, id);
		status = Status.RUNNING;
		core.run();
		status = Status.DONE;
	}

	public void pause() {
		if (status.equals(Status.RUNNING)) {
			status = Status.PAUSED;
			core.pause();
		}
	}

	public void play() {
		switch (status) {
			case PAUSED:
			case QUEUED:
				status = Status.RUNNING;
				core.play();
		}
	}

	public void stop() {
		core.stop();
		status = Status.CANCELLED;
	}

	public void reset() {
		stop();
	}

	enum Status {
		DONE, PAUSED, RUNNING, QUEUED, CANCELLED
	}
}
