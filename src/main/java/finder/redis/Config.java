package finder.redis;

import finder.controller.FinderController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

public class Config {

	@Autowired FinderController finderController;

	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
		return new JedisConnectionFactory();
	}

	@Bean
	RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(jedisConnectionFactory());
		return template;
	}

	@Bean
	MessageListenerAdapter messageListener() {
		return new MessageListenerAdapter(finderController);
	}

	@Bean
	Publisher messagePublisher() {
		return new finder.redis.Publisher(redisTemplate(), topic());
	}

	@Bean
	RedisMessageListenerContainer redisContainer() {
		return new RedisMessageListenerContainer() {{
			setConnectionFactory(jedisConnectionFactory());
			addMessageListener(messageListener(), topic());
		}};
	}

	@Bean
	private ChannelTopic topic() {
		return new ChannelTopic("updates");
	}

}
