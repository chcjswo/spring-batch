package me.mocadev.springbatch.part3;

import java.util.ArrayList;
import java.util.List;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-11
 **/
@Configuration
@Slf4j
@RequiredArgsConstructor
public class ItemProcessorConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job itemProcessorJob() {
		return jobBuilderFactory.get("itemProcessorJob")
			.incrementer(new RunIdIncrementer())
			.start(this.itemProcessorStep())
			.build();
	}

	@Bean
	public Step itemProcessorStep() {
		return stepBuilderFactory.get("itemProcessorStep")
			.<Person, Person>chunk(10)
			.reader(itemReader())
			.processor(itemProcessor())
			.writer(itemWriter())
			.build();
	}

	private ItemWriter<? super Person> itemWriter() {
		return items -> items.forEach(x -> log.info("PERSON.ID: {}", x.getId()));
	}

	private ItemProcessor<? super Person, ? extends Person> itemProcessor() {
		return item -> {
			if (item.getId() % 2 == 0) {
				return item;
			}
			return null;
		};
	}

	private ItemReader<Person> itemReader() {
		return new CustomItemReader<>(getItems());
	}

	private List<Person> getItems() {
		List<Person> items = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			int index = i + 1;
			items.add(new Person(index, "test name " + index, "age " + index, "address " + index));
		}
		return items;
	}

}
