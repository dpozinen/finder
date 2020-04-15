package finder;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

public class GeneralTest {

	@Test
	public void general() {
		WebClient.create()
				 .post()
				 .uri("http://localhost:8080/finder/query")
				 .body(BodyInserters.fromValue(Map.of(
				 	"threads", "2",
				 	"url", "https://www.baeldung.com/spring-5-webclient",
				 	"find", "baeldung",
				 	"maxUrls", "40"
				 )))
				 .exchange()
				 .block();
	}
}
