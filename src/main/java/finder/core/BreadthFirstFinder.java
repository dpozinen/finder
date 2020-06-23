package finder.core;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

public class BreadthFirstFinder extends Finder {

	public BreadthFirstFinder(Input input, finder.redis.Publisher redisTemplate) {
		super(input, redisTemplate);
	}

	@Override protected void find() {
		while (proceed()) {
			waitIfPaused();
			Page page = next();
			page.awaitResponse();

			Set<Page> following = page.findUrls();
			append(following);
			spreadMessage(following);
		}
	}
}
