package finder.core;

import lombok.Value;

public @Value class Input {
	private final String what;
	private final String url;
	private final short threads;
	private final short maxUrls;
}
