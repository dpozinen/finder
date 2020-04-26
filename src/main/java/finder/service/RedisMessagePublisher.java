package finder.service;

import finder.model.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RedisMessagePublisher {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ChannelTopic topic;

	public RedisMessagePublisher(RedisTemplate<String, Object> redisTemplate, ChannelTopic topic) {
		this.redisTemplate = redisTemplate;
		this.topic = topic;
	}

	public void publishAdd(String jobId, Page page) {
		redisTemplate.convertAndSend(topic.getTopic(), join(jobId, List.of(page), false));
	}

	private Map<String, ?> join(String jobId, Collection<?> pages, boolean refresh) {
		return Map.of("job", jobId,
					  "pages", pages,
					  "refresh", refresh);
	}

	public void publishAdd(String jobId, Collection<Page> pages) {
		redisTemplate.convertAndSend(topic.getTopic(), join(jobId, pages, false));
	}

	public void publishRefresh(String jobId, Collection<Object> members) {
		redisTemplate.convertAndSend(topic.getTopic(), join(jobId, members, true));
	}
}
