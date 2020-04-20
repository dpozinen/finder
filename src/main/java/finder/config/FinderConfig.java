package finder.config;

import finder.core.BreadthFirstCore;
import finder.core.Input;
import finder.model.Job;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.function.Function;

@Configuration
public class FinderConfig {

	@Bean
	public Function<Input, BreadthFirstCore> finderFactory() {
		return this::breadthFirstCode;
	}

	@Bean @Scope("prototype")
	public BreadthFirstCore breadthFirstCode(Input input) {
		return new BreadthFirstCore(input);
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
