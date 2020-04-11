package finder.controller;

import finder.model.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@EnableScheduling
@Controller
public class FinderController {

	@Autowired
	private SimpMessagingTemplate template;

	@Scheduled(fixedRate = 4000)
	public void refresh() {
		template.convertAndSend("/updates", new Page(1L, "1", 1L, 2));
	}

}
