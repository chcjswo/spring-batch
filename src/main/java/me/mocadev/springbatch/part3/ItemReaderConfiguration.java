package me.mocadev.springbatch.part3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-04
 **/
@Configuration
@Slf4j
@RequiredArgsConstructor
public class ItemReaderConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job itemReaderJob() throws Exception {
		return jobBuilderFactory.get("itemReaderJob")
			.incrementer(new RunIdIncrementer())
			.start(this.customItemReaderStep())
			.next(this.csvFileStep())
			.build();
	}

	private Step customItemReaderStep() {
		return stepBuilderFactory.get("customItemReaderStep")
			.<Person, Person>chunk(10)
			.reader(new CustomItemReader<>(getItems()))
			.writer(itemWriter())
			.build();
	}

	@Bean
	public Step csvFileStep() throws Exception {
		return stepBuilderFactory.get("csvFileStep")
			.<Person, Person>chunk(10)
			.reader(this.csvFileItemReader())
			.writer(itemWriter())
			.build();
	}

	private ItemWriter<Person> itemWriter() {
		return items -> log.info(items.stream()
			.map(Person:: getName)
			.collect(Collectors.joining(",")));
	}

	private FlatFileItemReader<Person> csvFileItemReader() throws Exception {
		DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
		final DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
		delimitedLineTokenizer.setNames("id", "name", "age", "address");
		lineMapper.setLineTokenizer(delimitedLineTokenizer);
		lineMapper.setFieldSetMapper(fieldSet -> {
			final int id = fieldSet.readInt("id");
			final String name = fieldSet.readString("name");
			final String age = fieldSet.readString("age");
			final String address = fieldSet.readString("address");

			return new Person(id, name, age, address);
		});

		final FlatFileItemReader<Person> itemReader = new FlatFileItemReaderBuilder<Person>()
			.name("csvFileItemReader")
			.encoding("UTF-8")
			.resource(new ClassPathResource("test.csv"))
			.linesToSkip(1)
			.lineMapper(lineMapper)
			.build();
		itemReader.afterPropertiesSet();;
		return itemReader;
	}
	private List<Person> getItems() {
		List<Person> items = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			items.add(new Person(1, "test name " + i, "test age", "test address"));
		}
		return items;
	}

}
