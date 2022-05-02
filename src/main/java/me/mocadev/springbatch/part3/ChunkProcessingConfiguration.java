package me.mocadev.springbatch.part3;

import io.micrometer.core.instrument.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-02
 **/
@Configuration
@Slf4j
@RequiredArgsConstructor
public class ChunkProcessingConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job chunkProcessingJob() {
		return jobBuilderFactory.get("chunkProcessingJob")
			.incrementer(new RunIdIncrementer())
			.start(this.taskBaseStep())
			.next(this.chunkBaseStep(null))
			.build();
	}

	private Step taskBaseStep() {
		return stepBuilderFactory.get("taskBaseStep")
			.tasklet(this.tasklet())
			.build();
	}

	private Tasklet tasklet() {
		List<String> items = getItems();
		return ((contribution, chunkContext) -> {
			final StepExecution stepExecution = contribution.getStepExecution();
			final JobParameters jobParameters = stepExecution.getJobParameters();
			String value = jobParameters.getString("chunkSize", "10");
			int chunkSize = StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : 10;
			final int fromIndex = stepExecution.getReadCount();
			int toIndex = fromIndex + chunkSize;
			if (fromIndex >= items.size()) {
				return RepeatStatus.FINISHED;
			}
			final List<String> subList = items.subList(fromIndex, toIndex);
			log.info("task item size: {}", Objects.requireNonNull(subList).size());
			stepExecution.setReadCount(toIndex);
			return RepeatStatus.CONTINUABLE;
		});
	}

	private List<String> getItems() {
		List<String> items = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			items.add(i + " Hello");
		}
		return items;
	}

	@Bean
	@JobScope
	public Step chunkBaseStep(@Value("#{jobParameters[chunkSize]}") String chunkSize) {
		return stepBuilderFactory.get("chunkBaseStep")
			.<String, String>chunk(StringUtils.isNotEmpty(chunkSize) ? Integer.parseInt(chunkSize) : 10)
			.reader(itemReader())
			.processor(itemProcessor())
			.writer(itemWriter())
			.build();
	}

	private ItemWriter<String> itemWriter() {
		return items -> log.info("chunk item size: {}", items.size());
//		return items -> items.forEach(log::info);
	}

	private ItemProcessor<String, String> itemProcessor() {
		return item -> item + ", Spring Batch";
	}

	private ItemReader<String> itemReader() {
		return new ListItemReader<>(getItems());
	}

}
