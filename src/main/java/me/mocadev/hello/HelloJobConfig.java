package me.mocadev.hello;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HelloJobConfig {

	private final JobBuilderFactory jobBuilderFactory;

	private final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job helloJob() {
		return jobBuilderFactory.get("helloworldJob2")
			.incrementer(new RunIdIncrementer())
			.start(helloStep())
			.build();
	}

	@JobScope
	@Bean
	public Step helloStep() {
		return stepBuilderFactory.get("helloworldStep2")
			.tasklet(helloTasklet())
			.build();
	}

	@StepScope
	@Bean
	public Tasklet helloTasklet() {
		return (stepContribution, chunkContext) -> {
            System.out.println("HelloJobConfig >>");
            return RepeatStatus.FINISHED;
        };
	}
}
