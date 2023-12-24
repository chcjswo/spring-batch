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
public class HelloConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job helloJob() {
		return jobBuilderFactory.get("helloJob")
			.incrementer(new RunIdIncrementer())
			.start(this.helloStep())
			.next(this.helloStep2())
			.next(this.helloStep3())
			.build();
	}

	@Bean
	public Step helloStep() {
		return stepBuilderFactory.get("helloStep")
			.tasklet(((contribution, chunkContext) -> {
				log.info("hello spring batch");
				return RepeatStatus.FINISHED;
			})).build();
	}

	@Bean
	public Step helloStep2() {
		return stepBuilderFactory.get("helloStep2")
			.tasklet(((contribution, chunkContext) -> {
				log.info("hello spring batch2");
				return RepeatStatus.FINISHED;
			})).build();
	}

	@Bean
	public Step helloStep3() {
		return stepBuilderFactory.get("helloStep3")
			.tasklet(((contribution, chunkContext) -> {
				log.info("hello spring batch3");
				return RepeatStatus.FINISHED;
			})).build();
	}
}
