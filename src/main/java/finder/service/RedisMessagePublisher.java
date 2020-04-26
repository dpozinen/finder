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
		redisTemplate.convertAndSend(topic.getTopic(), toMessage(jobId, List.of(page), List.of()));
	}

	public void publishAdd(String jobId, Collection<Page> pages) {
		redisTemplate.convertAndSend(topic.getTopic(), toMessage(jobId, pages, List.of()));
	}

	public void publishUpdate(String jobId, Collection<Page> pages) {
		redisTemplate.convertAndSend(topic.getTopic(), toMessage(jobId, List.of(), pages));
	}

	private Map<String, ?> toMessage(String jobId, Collection<Page> newPages, Collection<Page> updatedPages) {
		return Map.of("job", jobId,
					  "newPages", newPages,
					  "updatedPages", updatedPages);
	}

}
