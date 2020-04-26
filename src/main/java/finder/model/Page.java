package finder.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import org.jsoup.Jsoup;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@RedisHash("Page") @JsonTypeName("page")
public final @Data class Page {

	@Id private String id;
	private  String url;
	private  String domain;

	private  Long level;
	private Status status = Status.QUEUED;

	@JsonIgnore
	private String content;
	private int statusCode;
	private String errorMsg;

	@JsonIgnore
	private CountDownLatch responseReceivedSignal;

	Page(){}

	public Page(String url, String domain, long level) {
		this.url = url;
		this.domain = domain;
		this.level = level;
	}

	public void request(WebClient webClient) {
		var url = makeUrl();
		BiFunction<Integer, String, Mono<? extends String>> onErr = (code, msg) -> {
			statusCode = code;
			errorMsg = msg;
			return Mono.just("");
		};
		content = webClient.get().uri(url)
						   .retrieve()
						   .bodyToMono(String.class)
						   .onErrorResume(WebClientResponseException.class,
										  e -> {
											  statusCode = e.getRawStatusCode();
											  return Mono.just("");
										  })
						   .onErrorResume(DataBufferLimitException.class,
										  e -> onErr.apply(-1, "Response size too large"))
						   .onErrorResume(Throwable.class,
										  e -> onErr.apply(-1, "Random Exception"))
						   .block();
		if (statusCode == 0) statusCode = 200;
		responseReceivedSignal.countDown();
	}

	private String makeUrl() {
		try {
			var uri = new URI(url);
			if (!uri.isAbsolute())
				return new URI("https://" + domain + (url.startsWith("/") ? url : "/" + url)).toString();
		} catch (URISyntaxException e) {
			status = Status.ERROR;
			errorMsg = "Malformed url";
		}
		return url;
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
		if (content == null) return new HashSet<>();
		return Jsoup.parse(content)
					.select("a[href]").eachAttr("href").stream()
					.filter(s -> !s.isBlank())
					.map(href -> new Page(href, domain, this.level + 1))
					.collect(Collectors.toSet());
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Page && ((Page) o).url.equals(url) && ((Page) o).domain.equals(domain);
	}

	@Override
	public int hashCode() {
		return url.hashCode() + domain.hashCode();
	}

	public enum Status {
		FOUND, ABSENT, QUEUED, ERROR, CANCELLED
	}
}
