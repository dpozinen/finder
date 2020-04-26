package finder.config;

import finder.core.BreadthFirstFinder;
import finder.core.Input;
import finder.model.Job;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.function.BiFunction;
import java.util.function.Function;

@Configuration
public class FinderConfig {

	@Bean
	public BiFunction<Input, String, BreadthFirstFinder> finderFactory() {
		return this::breadthFirstFinder;
	}

	@Bean @Scope("prototype")
	public BreadthFirstFinder breadthFirstFinder(Input input, String jobId) {
		return new BreadthFirstFinder(input, jobId);
	}

	@Bean
	public Function<Input, Job> jobFactory() {
		return this::job;
	}

	@Bean @Scope("prototype")
	public Job job(Input input) {
		return new Job(input);
	}

}
