package finder;

import finder.api.Job;
import finder.core.Finder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

public class JobTest {

	@Test
	public void lifecycle() throws InterruptedException {
		ConcurrentHashMap<String, Future<?>> jobFutures = new ConcurrentHashMap<>();
		ThreadPoolExecutor jobPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

		var input = new Finder.Input("lol", "https://www.google.com", 300, 2, null);
		var job = new Job<>(input);
		Future<?> future = jobPool.submit(job::run);
		job.setId("1");
		jobFutures.put(job.getId(), future);

		Assertions.assertEquals(job.getState(), Job.State.RUNNING);
		Thread.sleep(5_000);
		job.pause();
		Assertions.assertEquals(job.getState(), Job.State.PAUSED);
		Thread.sleep(5_000);
		job.stop();
		future.cancel(true);
		Assertions.assertEquals(job.getState(), Job.State.STOPPED);
//		Assertions.assertEquals(jobPool.run, Job.State.STOPPED);
		Thread.sleep(5_000);
		job.run();
		Assertions.assertEquals(job.getState(), Job.State.RUNNING);
	}

}
