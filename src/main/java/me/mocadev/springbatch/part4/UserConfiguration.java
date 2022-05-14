package me.mocadev.springbatch.part4;

import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
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

	public static final int PAGE_SIZE = 100;
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final UserRepository userRepository;
	private final EntityManagerFactory entityManagerFactory;

	@Bean
	public Job userJob() throws Exception {
		return jobBuilderFactory.get("userJob")
			.incrementer(new RunIdIncrementer())
			.start(this.saveUserStep())
			.next(this.userLevelUpStep())
			.build();
	}

	@Bean
	public Step saveUserStep() {
		return stepBuilderFactory.get("saveUserStep")
			.tasklet(new SaveUserTaskLet(userRepository))
			.build();
	}

	@Bean
	public Step userLevelUpStep() throws Exception {
		return stepBuilderFactory.get("userLevelUp")
			.<User, User>chunk(PAGE_SIZE)
			.reader(itemReader())
			.processor(itemProcessor())
			.writer(itemWriter())
			.build();
	}

	private ItemWriter<? super User> itemWriter() {
		return users -> users.forEach(x -> {
			x.levelUp();
			userRepository.save(x);
		});
	}

	private ItemProcessor<? super User, ? extends User> itemProcessor() {
		return user -> {
			if (user.availableLevelUp()) {
				return user;
			}
			return null;
		};
	}

	private ItemReader<? extends User> itemReader() throws Exception {
		final JpaPagingItemReader<User> itemReader = new JpaPagingItemReaderBuilder<User>()
			.queryString("select u from User u")
			.entityManagerFactory(entityManagerFactory)
			.pageSize(PAGE_SIZE)
			.name("userItemReader")
			.build();
		itemReader.afterPropertiesSet();
		return itemReader;
	}

}
