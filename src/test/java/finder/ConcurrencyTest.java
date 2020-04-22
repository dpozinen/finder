package finder;

import finder.core.concurrent.Pause;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConcurrencyTest {

	private final static Logger log = LoggerFactory.getLogger(ConcurrencyTest.class);

	@Test
	public void pauseTest() {
		Pause pause = new Pause();
		ExecutorService pausePool = Executors.newFixedThreadPool(7);

		var runnables = Stream.iterate(1, i -> ++i)
							  .limit(30)
							  .map((i) -> (Runnable) () -> {
								  log.info("Starting job: {}", i);
								  pause.await();
								  log.info("Competing job: {}", i);
							  })
							  .collect(Collectors.toList());

		try {
			log.info("starting jobs");
			for (Runnable callable : runnables) pausePool.execute(callable);
			pause.pause();
			Thread.sleep(10_000);
			pause.resume();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			log.info("Shutting Down");
			pausePool.shutdown();
		}

	}
}
