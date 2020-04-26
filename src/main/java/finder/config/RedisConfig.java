package finder.config;

import finder.controller.FinderController;
import finder.service.RedisMessagePublisher;
import finder.service.RedisMessageSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

@Configuration
public class RedisConfig {

	@Autowired FinderController finderController;

	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
		return new JedisConnectionFactory();
	}

	@Bean
	RedisTemplate<String, Object> redisTemplate() {
		return new RedisTemplate<>() {{
			setConnectionFactory(jedisConnectionFactory());
			setDefaultSerializer(redisSerializer());
		}};
	}

	@Bean Jackson2JsonRedisSerializer<Object> redisSerializer() {
		return new Jackson2JsonRedisSerializer<>(Object.class);
	}

	@Bean
	MessageListenerAdapter messageListener() {
		return new MessageListenerAdapter(new RedisMessageSubscriber());
	}

	@Bean
	RedisMessageListenerContainer redisContainer() {
		return new RedisMessageListenerContainer() {{
			setConnectionFactory(jedisConnectionFactory());
			addMessageListener(finderController, topic());
		}};
	}

	@Bean
	RedisMessagePublisher redisPublisher() {
		return new RedisMessagePublisher(redisTemplate(), topic());
	}

	@Bean
	ChannelTopic topic() {
		return new ChannelTopic("update");
	}

}
