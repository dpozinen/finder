package finder.controller;

import finder.service.FinderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@EnableScheduling
@Controller
public class FinderController {

	private final SimpMessagingTemplate template;
	private final FinderService service;

	public FinderController(SimpMessagingTemplate template, FinderService service) {
		this.template = template;
		this.service = service;
	}

	@Scheduled(fixedRate = 4000)
	public void refresh() {
		template.convertAndSend("/updates", service.update("1"));
	}

	@PostMapping("/finder/query")
	public String query(@RequestBody Map<String, String> form) {
		return service.find(form);
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
