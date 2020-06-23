package finder;

import finder.repo.JobRepo;
import finder.service.FinderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@SpringJUnitConfig
public class GeneralTest {

	private @MockBean JobRepo jobRepo;
	private FinderService service;

	@BeforeAll
	private void init() {
		service = new FinderService(jobRepo, null);
	}

	@Test
	public void general() {
		WebClient.create()
				 .post()
				 .uri("http://localhost:8080/finder/query")
				 .body(BodyInserters.fromValue(Map.of(
				 	"threads", "2",
				 	"url", "https://www.github.com/dpozinen",
				 	"find", "dpozinen",
				 	"maxUrls", "40"
				 )))
				 .exchange()
				 .block();
	}

	@Test
	public void aVoid() {
		var map = Map.of(
				"maxThreads", "3",
				"maxUrls", "4",
				"startingUrl", "https://google.com",
				"search", "com"
		);
		service.addJob(map);
		Assertions.assertEquals(1, jobRepo.count());
	}
}
