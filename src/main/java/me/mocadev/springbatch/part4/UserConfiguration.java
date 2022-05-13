package me.mocadev.springbatch.part4;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-14(014)
 **/
@Configuration
@Slf4j
@RequiredArgsConstructor
public class UserConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final UserRepository userRepository;

	@Bean
	public Job userJob() {
		return jobBuilderFactory.get("userJob")
			.incrementer(new RunIdIncrementer())
			.start(this.saveUserStep())
			.build();
	}

	@Bean
	public Step saveUserStep() {
		return stepBuilderFactory.get("saveUserStep")
			.tasklet(new SaveUserTaskLet(userRepository))
			.build();
	}

}
