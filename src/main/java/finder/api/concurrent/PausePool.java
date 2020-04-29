package finder.api.concurrent;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class PausePool extends ScheduledThreadPoolExecutor {

	private final Pause pause;

	public PausePool(int corePoolSize, Pause pause) {
		super(corePoolSize);
		this.pause = pause;
	}

	@Override protected void beforeExecute(Thread t, Runnable r) {
		pause.await();
		super.beforeExecute(t, r);
	}
}
