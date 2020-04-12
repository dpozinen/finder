package finder.controller;

import finder.service.FinderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
		template.convertAndSend("/updates", service.update());
	}

	@PostMapping("/finder/query")
	public void query(@RequestBody Map<String, String> form) {
		service.find(form);
	}
}
