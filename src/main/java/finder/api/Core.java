package finder.api;

public interface Core<R> {
	void run();
	void stop();
	void pause();
	void cancel();
	R results();
}
