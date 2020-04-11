package finder.model;

import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
public @Data class Page {

	private Long id;
	private final String url;
	private final Long level;
	private long urlsFound;

}
