package finder.core;

import finder.api.Core;
import finder.api.Job;

import java.util.Map;
import java.util.Set;

public record FinderInput(String searchStr, String startingUrl, int maxUrls, int maxThreads)
		implements Job.Input<Set<Page>> {

	public static boolean validate(Map<String, String> form) {
		return false;
	}

	@Override public Core<Set<Page>> core() {
		return null;
	}

}
