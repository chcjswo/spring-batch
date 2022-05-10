package me.mocadev.springbatch.part3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
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
	private final DataSource dataSource;
	private final EntityManagerFactory entityManagerFactory;

	@Bean
	public Job itemReaderJob() throws Exception {
		return jobBuilderFactory.get("itemReaderJob")
			.incrementer(new RunIdIncrementer())
			.start(this.customItemReaderStep())
			.next(this.csvFileStep())
			.next(this.jdbcStep())
			.next(this.jpaStep())
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

	@Bean
	public Step jdbcStep() throws Exception {
		return stepBuilderFactory.get("jdbcStep")
			.<Person, Person>chunk(10)
			.reader(this.jdbcCursorItemReader())
			.writer(itemWriter())
			.build();
	}

	@Bean
	public Step jpaStep() throws Exception {
		return stepBuilderFactory.get("jpaStep")
			.<Person, Person>chunk(10)
			.reader(this.jpaCursorItemReader())
			.writer(itemWriter())
			.build();
	}

	private JpaCursorItemReader<Person> jpaCursorItemReader() throws Exception {
		final JpaCursorItemReader<Person> itemReader = new JpaCursorItemReaderBuilder<Person>()
			.name("jpaCursorItemReader")
			.entityManagerFactory(entityManagerFactory)
			.queryString("select p from person p")
			.build();
		itemReader.afterPropertiesSet();
		return itemReader;
	}

	private JdbcCursorItemReader<Person> jdbcCursorItemReader() throws Exception {
		final JdbcCursorItemReader<Person> itemReader = new JdbcCursorItemReaderBuilder<Person>()
			.name("jdbcCursorItemReader")
			.dataSource(dataSource)
			.sql("select id, name, age, address from person")
			.rowMapper(((rs, rowNum) -> new Person(rs.getInt(1), rs.getString(2), rs.getString(3),
				rs.getString(4))))
			.build();
		itemReader.afterPropertiesSet();
		return itemReader;
	}

	private ItemWriter<Person> itemWriter() {
		return items -> log.info(items.stream()
			.map(Person::getName)
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
		itemReader.afterPropertiesSet();
		;
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
