package finder.core;

import finder.model.Page;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class BreadthFirstFinder extends Finder {

	public BreadthFirstFinder(Input input, String jobId) {
		super(input, jobId);
	}

	@Override
	public void run() {
		String domain = getDomain();
		var startingPage = new Page(input.getUrl(), domain, 0L);

		var visited = new HashSet<Page>();
		var queue = new LinkedList<Page>();

		addNewPage(startingPage);
		queue.add(startingPage);

		submitForRequest(startingPage);

		while (!queue.isEmpty() && !stop) {
			Page page = queue.poll();

			pause.await();

			await(page); visited.add(page);

			handleNewUrls(visited, queue, page);

			page.find(input.getWhat());
			break;
		}

		cancelQueued();

		executor.shutdownNow();
	}

	protected void handleNewUrls(HashSet<Page> visited, LinkedList<Page> queue, Page page) {
		Set<Page> urls = page.findUrls();
		urls.removeAll(visited);

		var newPages = new HashSet<Page>();

		for (Page url : urls) {
			if (visited.size() + queue.size() + newPages.size() >= input.getMaxUrls())
				break;
			newPages.add(url);
		}

		queue.addAll(newPages);

		addNewPages(newPages);

		newPages.forEach(this::submitForRequest);
//		log.info("Submitted {} urls for request", urls.size());
	}

}
