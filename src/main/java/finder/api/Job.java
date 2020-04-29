package finder.api;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Optional;

@RedisHash("Job")
public class Job<R> {

	@Getter
	@Id private String id;
	private final Core<R> core;
	@Getter
	private State state;

	public Job(Input<R> input) {
		core = input.core();
	}

	public Optional<R> results() {
		return state.equals(State.RUNNING) ? Optional.empty() : Optional.ofNullable(core.results());
	}

	public boolean pause() {
		return changeStateIf(State.RUNNING, core::pause, State.PAUSED);
	}

	public boolean stop() {
		return changeStateIf(State.RUNNING, core::run, State.STOPPED);
	}

	public boolean resume() {
		State change = State.RUNNING;
		return changeStateIf(State.PAUSED, core::run, change) || changeStateIf(State.STOPPED, core::run, change);
	}

	public boolean cancel() {
		core.cancel();
		state = State.CANCELLED;
		return true;
	}

	public boolean run() {
		if (state.equals(State.QUEUED)) {
			state = State.RUNNING;
			core.run();
			state = State.DONE;
			return true;
		}
		return false;
	}

	private boolean changeStateIf(State toCheck, Runnable action, State change) {
		boolean equal = this.state.equals(toCheck);
		if (equal) {
			action.run();
			this.state = change;
		}
		return equal;
	}

	public interface Input<R> {
		Core<R> core();
	}

	public enum State {
		RUNNING, PAUSED, DONE, CANCELLED, STOPPED, QUEUED
	}

}
