package finder.service;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class PausePool extends ScheduledThreadPoolExecutor {

	private final Pause pause;

	public PausePool(int corePoolSize, Pause p) {
		super(corePoolSize);
		pause = p;
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		pause.await();
		super.beforeExecute(t, r);
	}

}
