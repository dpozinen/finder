package finder.core;

import finder.api.Core;
import finder.api.Job;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Set;

public abstract class Finder implements Core<Set<Page>> {

	protected final WebClient webClient = WebClient.create();

	public static record Input(String search, String startingUrl, int maxUrls, int maxThreads)
			implements Job.Input<Set<Page>> {

		public static boolean validate(Map<String, String> form) {
			String maxThreads = form.get("maxThreads");
			String maxUrls = form.get("maxUrls");
			String startingUrl = form.get("startingUrl");
			String searchStr = form.get("search");

			return false;
		}

		@Override public Core<Set<Page>> core() {
			return null;
		}

	}

}
