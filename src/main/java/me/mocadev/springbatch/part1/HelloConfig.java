package me.mocadev.springbatch.part1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-04-30
 **/
@Configuration
@Slf4j
@RequiredArgsConstructor
public class HelloConfig {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job helloJob2() {
		return jobBuilderFactory.get("helloworldJob")
			.incrementer(new RunIdIncrementer())
			.start(this.helloStep11())
			.next(this.helloStep22())
			.next(this.helloStep33())
			.build();
	}

	@Bean
	public Step helloStep11() {
		return stepBuilderFactory.get("helloStep")
			.tasklet(((contribution, chunkContext) -> {
				log.info("hello spring batch");
				return RepeatStatus.FINISHED;
			})).build();
	}

	@Bean
	public Step helloStep22() {
		return stepBuilderFactory.get("helloStep2")
			.tasklet(((contribution, chunkContext) -> {
				log.info("hello spring batch2");
				return RepeatStatus.FINISHED;
			})).build();
	}

	@Bean
	public Step helloStep33() {
		return stepBuilderFactory.get("helloStep3")
			.tasklet(((contribution, chunkContext) -> {
				log.info("hello spring batch3");
				return RepeatStatus.FINISHED;
			})).build();
	}
}
