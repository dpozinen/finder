package finder.service;

import org.springframework.web.reactive.function.client.WebClient;

public class Finder {

	private final Input input;

	public Finder(Input input) {
		this.input = input;
	}

	public boolean find() {
		try {
			WebClient webClient = WebClient.create(input.url);
			String response = webClient.get().retrieve().bodyToMono(String.class).block();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	static class Input {
		private final String what;
		private final String url;
		private final short threads;

		Input(String what, String url, short threads) {
			this.what = what;
			this.url = url;
			this.threads = threads;
		}
	}
}
