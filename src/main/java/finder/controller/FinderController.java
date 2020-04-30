package finder.controller;

import finder.api.Job;
import finder.core.Page;
import finder.service.FinderService;
import lombok.SneakyThrows;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.Set;

@Controller
public class FinderController implements MessageListener {

	private final FinderService service;
	private final SimpMessagingTemplate messagingTemplate;

	public FinderController(FinderService service, SimpMessagingTemplate messagingTemplate) {
		this.service = service;
		this.messagingTemplate = messagingTemplate;
	}

	@PostMapping("/job/add")
	public Map<String, String> addJob(@RequestBody Map<String, String> form) {
		var id = service.addJob(form);
		return Map.of("id", id);
	}

	@GetMapping("/job/results")
	public Set<Page> results(String id) {
		return service.results(id).orElse(Set.of());
	}

	@GetMapping("/job/results")
	public ResponseEntity<Map<String, Job.State>> state(String id) {
		return service.state(id)
					  .map(s -> new ResponseEntity<>(Map.of("state", s), HttpStatus.OK))
					  .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@SneakyThrows @Override
	public void onMessage(Message message, byte[] pattern) {
		String body = new String(message.getBody());

		String jobId = cutJobId(body);
		Thread.sleep(2000); // delay
		messagingTemplate.convertAndSend("/updates/" + jobId, body);
	}

	private String cutJobId(String str) {
		int start = str.indexOf("job\":\"");
		if (start != -1) {
			int end = str.indexOf("\"", start + "job\":\"".length());
			if (end != -1) return str.substring(start + "job\":\"".length(), end);
		}
		return "";
	}
}
