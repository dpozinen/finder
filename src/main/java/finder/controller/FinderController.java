package finder.controller;

import finder.model.Job;
import finder.service.FinderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@EnableScheduling
@Controller
public class FinderController {

	private final SimpMessagingTemplate template;
	private final FinderService service;

	public FinderController(SimpMessagingTemplate template, FinderService service) {
		this.template = template;
		this.service = service;
	}

	@GetMapping("/jobs/{id}")
	public ResponseEntity<Job> job(@RequestAttribute String id) {
		return service.getJob(id)
					  .map(j -> new ResponseEntity<>(j, HttpStatus.OK))
					  .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@Scheduled(fixedRate = 4000)
	public void refresh() {
		template.convertAndSend("/updates", service.update("1"));
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

}
