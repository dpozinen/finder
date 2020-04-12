package finder.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jsoup.Jsoup;
import org.jsoup.select.Collector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.stream.Collectors;

public final @Data class Page {

	private Long id;
	private String url;
	private final String domain;

	private final Long level;
	private Status status = Status.QUEUED;

	private String content;
	private int statusCode;
	private String errorMsg;

	public Page(String url, String domain, long level) {
		this.url = url;
		this.domain = domain;
		this.level = level;
	}

	public void request() {
		makeUrl();
		content = WebClient.create(url).get().retrieve()
						   .bodyToMono(String.class)
						   .onErrorResume(WebClientResponseException.class,
							   e -> {
									statusCode = e.getRawStatusCode();
									return Mono.justOrEmpty("");
						   })
						   .onErrorResume(Exception.class,
							   e -> {
								   statusCode = -1;
								   errorMsg = "Random Exception";
								   return Mono.justOrEmpty("");
							   })
						   .block();
		if (statusCode == 0) statusCode = 200;
	}

	private void makeUrl() {
		try {
			var uri = new URI(url);
			if (!uri.isAbsolute())
				url = new URI("https://" + domain + "/" + (url.startsWith("/") ? url : "/"+url)).toString();
		} catch (URISyntaxException e) {
			status = Status.ERROR;
			errorMsg = "Malformed url";
		}
	}

	public void find(String what) {
		if (statusCode != 200)
			status = Status.ERROR;
		else if (content != null && content.contains(what))
			status = Status.FOUND;
		else
			status = Status.ABSENT;
	}

	public Set<Page> findUrls() {
		if (content == null) return Set.of();
		return Jsoup.parse(content)
					.select("a[href]").eachAttr("href").stream()
					.filter(s -> !s.isBlank())
					.map(href -> new Page(href, domain, this.level + 1))
					.collect(Collectors.toSet());
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Page && (((Page)o).url.equals(url) && ((Page)o).domain.equals(domain));
	}

	@Override
	public int hashCode() {
		return url.hashCode() + domain.hashCode();
	}

}
