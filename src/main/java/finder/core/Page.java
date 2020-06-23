package finder.core;

import org.springframework.web.reactive.function.client.WebClient;

import java.util.Set;

public class Page {
	private final String url;

	public Page(String startingUrl) {
		url = startingUrl;
	}

	public void request(WebClient client) {

	}

	public void awaitResponse() {

	}

	public Set<Page> findUrls() {
		return Set.of();
	}
}
