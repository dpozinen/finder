package finder.controller;

import finder.model.Job;
import finder.service.FinderService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@EnableScheduling
@Controller
public class FinderController implements MessageListener {

	private final SimpMessagingTemplate template;
	private final FinderService service;
	private final Jackson2JsonRedisSerializer<Object> redisSerializer;

	public FinderController(SimpMessagingTemplate template, FinderService service, Jackson2JsonRedisSerializer<Object> redisSerializer) {
		this.template = template;
		this.service = service;
		this.redisSerializer = redisSerializer;
	}

	@GetMapping("/jobs/{id}")
	public ResponseEntity<Job> job(@RequestAttribute String id) {
		return service.getJob(id)
					  .map(j -> new ResponseEntity<>(j, HttpStatus.OK))
					  .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@PostMapping("/finder/query")
	public ResponseEntity<Map<String, String>> query(@RequestBody Map<String, String> form) {
		String id = service.find(form);
		return new ResponseEntity<>(Map.of("id", id), HttpStatus.OK);
	}

	@MessageMapping("/pause/{id}")
	public void pause(@RequestParam String id) {
		service.pause(id);
	}

	@MessageMapping("/play/{id}")
	public void play(@RequestParam String id) {
		service.play(id);
	}

	@MessageMapping("/stop/{id}")
	public void stop(@RequestParam String id) {
		service.stop(id);
	}

	@MessageMapping("/reset/{id}")
	public void reset(@RequestParam String id) {
		service.reset(id);
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String body = new String(message.getBody());

		String jobId = substringBetween(body, "job\":\"", "\"");
		template.convertAndSend("/updates/" + jobId, body);
	}

	public static String substringBetween(String str, String open, String close) { // TODO remove
		if (str != null && open != null && close != null) {
			int start = str.indexOf(open);
			if (start != -1) {
				int end = str.indexOf(close, start + open.length());
				if (end != -1) {
					return str.substring(start + open.length(), end);
				}
			}
		}
		return "";
	}
}
