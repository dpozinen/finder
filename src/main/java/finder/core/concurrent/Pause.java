package finder.core.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class Pause {
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition resume = lock.newCondition();
	private volatile boolean isPaused;

	public void pause() {
		lock.lock();
		try {
			log.info("..............Pausing..............");
			isPaused = true;
		} finally {
			lock.unlock();
		}
	}

	public void resume() {
		lock.lock();
		try {
			if (isPaused) {
				log.info("..............Resuming..............");
				isPaused = false;
				resume.signalAll();
			}
		} finally {
			lock.unlock();
		}
	}

	public void await() {
		lock.lock();
		try {
			while (isPaused) resume.await();
		} catch ( InterruptedException e ) {
			log.info("Thread interrupted while waiting");
		} finally {
			lock.unlock();
		}
	}

	public boolean isPaused() {
		return isPaused;
	}
}
