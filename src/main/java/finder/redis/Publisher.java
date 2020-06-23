package finder.redis;

import finder.core.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Publisher {
	private final RedisTemplate<String, Object> redisTemplate;
	private final ChannelTopic topic;

	public Publisher(RedisTemplate<String, Object> redisTemplate, ChannelTopic topic) {
		this.redisTemplate = redisTemplate;
		this.topic = topic;
	}

	private Map<String, ?> toMsg(String jobId, Collection<Page> add, Collection<Page> update) {
		return Map.of(
				"job", jobId,
				"add", add,
				"update", update
		);
	}

	public void publishAdd(String jobId, Collection<Page> pages) {
		Map<String, ?> msg = toMsg(jobId, pages, List.of());
		redisTemplate.convertAndSend(topic.getTopic(), msg);
	}

	public void publishUpdate(String jobId, Collection<Page> pages) {
		Map<String, ?> msg = toMsg(jobId, List.of(), pages);
		redisTemplate.convertAndSend(topic.getTopic(), msg);
	}
}
