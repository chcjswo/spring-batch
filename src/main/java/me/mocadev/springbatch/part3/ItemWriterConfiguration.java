package me.mocadev.springbatch.part3;

import io.micrometer.core.instrument.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-10
 **/
@Configuration
@Slf4j
@RequiredArgsConstructor
public class ItemWriterConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job itemWriterJob() throws Exception {
		return jobBuilderFactory.get("itemWriterJob")
			.incrementer(new RunIdIncrementer())
			.start(this.csvItemWriterStep())
			.build();
	}

	private Step csvItemWriterStep() throws Exception {
		return stepBuilderFactory.get("csvItemWriterStep")
			.<Person, Person>chunk(10)
			.reader(itemReader())
			.writer(csvFileItemWriter())
			.build();
	}

	private ItemWriter<Person> csvFileItemWriter() throws Exception {
		final BeanWrapperFieldExtractor<Person> fieldExtractor = new BeanWrapperFieldExtractor<>();
		fieldExtractor.setNames(new String[]{"id", "name", "age", "address"});
		final DelimitedLineAggregator<Person> lineAggregator = new DelimitedLineAggregator<>();
		lineAggregator.setDelimiter(",");
		lineAggregator.setFieldExtractor(fieldExtractor);

		final FlatFileItemWriter<Person> itemWriter = new FlatFileItemWriterBuilder<Person>()
			.name("csvFileItemWriter")
			.encoding("UTF-8")
			.resource(new FileSystemResource("output/test-output.csv"))
			.lineAggregator(lineAggregator)
			.headerCallback(writer -> writer.write("id,이름,나이,거주지"))
			.footerCallback(writer -> writer.write("----------------------------\n"))
			.append(true)
			.build();
		itemWriter.afterPropertiesSet();
		return itemWriter;
	}

	private ItemReader<Person> itemReader() {
		return new CustomItemReader<>(getItems());
	}

	private List<Person> getItems() {
		List<Person> items = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			items.add(new Person(i + 1, "test name " + i, "test age " + i, "test address " + i));
		}
		return items;
	}

}
