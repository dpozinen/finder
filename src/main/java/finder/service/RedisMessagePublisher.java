package finder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

public class RedisMessagePublisher {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ChannelTopic topic;

	public RedisMessagePublisher(RedisTemplate<String, Object> redisTemplate, ChannelTopic topic) {
		this.redisTemplate = redisTemplate;
		this.topic = topic;
	}

	public void publish(String message) {
		redisTemplate.convertAndSend(topic.getTopic(), message);
	}

}
