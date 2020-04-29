package finder.api.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class Pause {

	private final ReentrantLock lock = new ReentrantLock();
	private final Condition resume = lock.newCondition();
	private volatile boolean paused;

	public void pause() {
		try {
			lock.lock();
			log.info(".............PAUSING.............");
			paused = true;
		} finally {
			lock.unlock();
		}
	}

	public void resume() {
		if (paused) try {
			lock.lock();
			log.info(".............RESUMING.............");
			resume.signal();
			paused = false;
		} finally {
			lock.unlock();
		}
	}

	public void await() {
		if (paused) try {
			lock.lock();
			log.info(".............WAITING.............");
			resume.await();
		} catch (InterruptedException e) {
			log.warn("Interrupted while waiting for resume");
		} finally {
			lock.unlock();
		}
	}

	public boolean isPaused() {
		return paused;
	}
}
