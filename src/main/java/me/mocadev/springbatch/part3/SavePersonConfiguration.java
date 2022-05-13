package me.mocadev.springbatch.part3;

import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mocadev.springbatch.part3.SavePersonListener.SavePersonAnnotationJobExecutionListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-12(012)
 **/
@Configuration
@Slf4j
@RequiredArgsConstructor
public class SavePersonConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;

	@Bean
	public Job savePersonJob() throws Exception {
		return jobBuilderFactory.get("savePersonJob")
			.incrementer(new RunIdIncrementer())
			.start(this.savePersonStep(null))
			.listener(new SavePersonListener.SavePersonJobExecutionListener())
			.listener(new SavePersonAnnotationJobExecutionListener())
			.build();
	}

	@Bean
	@JobScope
	public Step savePersonStep(@Value("#{jobParameters[allow_duplicate]}") String allowDuplicate)
		throws Exception {
		return stepBuilderFactory.get("savePersonStep")
			.<Person, Person>chunk(10)
			.reader(itemReader())
//			.processor(new DuplicateValidationProcessor<>(Person::getName, Boolean.parseBoolean(allowDuplicate)))
			.processor(itemProcessor(null))
			.writer(itemWriter())
			.listener(new SavePersonListener.SavePersonStepExecutionListener())
			.faultTolerant()
			.skip(NotFoundNameException.class)
			.skipLimit(2)
			.build();
	}

	private ItemProcessor<? super Person, ? extends Person> itemProcessor(String allowDuplicate)
		throws Exception {
		DuplicateValidationProcessor<Person> duplicateValidationProcessor =
			new DuplicateValidationProcessor<>(Person::getName,
				Boolean.parseBoolean(allowDuplicate));

		ItemProcessor<Person, Person> validationProcessor = item -> {
			if (item.isNotEmptyName()) {
				return item;
			}

			throw new NotFoundNameException();
		};

		CompositeItemProcessor<Person, Person> itemProcessor = new CompositeItemProcessorBuilder<Person, Person>()
			.delegates(new PersonValidationRetryProcessor(), validationProcessor, duplicateValidationProcessor)
			.build();

		itemProcessor.afterPropertiesSet();

		return itemProcessor;
	}

	private ItemWriter<? super Person> itemWriter() throws Exception {
		final JpaItemWriter<Person> jpaItemWriter = new JpaItemWriterBuilder<Person>()
			.entityManagerFactory(entityManagerFactory)
			.build();
		final ItemWriter<Person> logItemWriter = items -> log.info("person.size: {}", items.size());

		final CompositeItemWriter<Person> itemWriter = new CompositeItemWriterBuilder<Person>()
			.delegates(jpaItemWriter, logItemWriter)
			.build();
		itemWriter.afterPropertiesSet();
		return itemWriter;
	}

	private ItemReader<? extends Person> itemReader() throws Exception {
		final DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
		final DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setNames("name", "age", "address");
		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(fieldSet -> new Person(
			fieldSet.readString(0),
			fieldSet.readString(1),
			fieldSet.readString(2)
		));
		final FlatFileItemReader<Person> itemReader = new FlatFileItemReaderBuilder<Person>()
			.name("savePersonItemReader")
			.encoding("UTF-8")
			.linesToSkip(1)
			.resource(new ClassPathResource("person.csv"))
			.lineMapper(lineMapper)
			.build();
		itemReader.afterPropertiesSet();
		return itemReader;
	}

}
