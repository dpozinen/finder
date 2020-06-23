package finder.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Optional;

@RedisHash("Job")
public class Job<R> {

	@Id @Getter @Setter
	private String id;
	private final Core<R> core;
	@Getter
	private State state = State.QUEUED;

	public Job(Input<R> input) {
		core = input.core();
	}

	public Optional<R> results() {
		return Optional.ofNullable(core.results());
	}

	public boolean pause() {
		return ifRunning(core::pause, State.PAUSED);
	}

	public boolean stop() {
		return ifRunning(core::stop, State.STOPPED);
	}

	public boolean cancel() {
		core.cancel();
		state = State.CANCELLED;
		return true;
	}

	public void run() {
		if (state.equals(State.QUEUED) || state.equals(State.PAUSED) || state.equals(State.STOPPED)) {
			state = State.RUNNING;
			core.run();
			state = State.DONE;
		}
	}

	private boolean ifRunning(Runnable action, State change) {
		boolean equal = this.state.equals(State.RUNNING);
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
