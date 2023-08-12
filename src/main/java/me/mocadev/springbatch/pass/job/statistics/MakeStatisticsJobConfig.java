package me.mocadev.springbatch.pass.job.statistics;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import me.mocadev.springbatch.pass.repository.booking.BookingEntity;
import me.mocadev.springbatch.pass.repository.statistics.StatisticsEntity;
import me.mocadev.springbatch.pass.repository.statistics.StatisticsRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@RequiredArgsConstructor
@Configuration
public class MakeStatisticsJobConfig {

	private static final int CHUNK_SIZE = 10;

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;
	private final StatisticsRepository statisticsRepository;
	private final MakeDailyStatisticsTasklet makeDailyStatisticsTasklet;
	private final MakeWeeklyStatisticsTasklet makeWeeklyStatisticsTasklet;

	@Bean
	public Job makeStatisticsJob() {
		Flow addStatisticsFlow = new FlowBuilder<Flow>("addStatisticsFlow")
			.start(addStatisticsStep())
			.build();

		Flow makeDailyStatisticsFlow = new FlowBuilder<Flow>("makeDailyStatisticsFlow")
			.start(makeDailyStatisticsStep())
			.build();

		Flow makeWeeklyStatisticsFlow = new FlowBuilder<Flow>("makeWeeklyStatisticsFlow")
			.start(makeWeeklyStatisticsStep())
			.build();

		Flow parallelFlow = new FlowBuilder<Flow>("parallelMakeStatisticsFlow")
			.split(new SimpleAsyncTaskExecutor())
			.add(makeDailyStatisticsFlow, makeWeeklyStatisticsFlow)
			.build();

		return jobBuilderFactory.get("makeStatisticsJob")
			.start(addStatisticsFlow)
			.next(parallelFlow)
			.build()
			.build();
	}

	@Bean
	public Step addStatisticsStep() {
		return stepBuilderFactory.get("addStatisticsStep")
			.<BookingEntity, BookingEntity>chunk(CHUNK_SIZE)
			.reader(addStatisticsItemReader(null, null))
			.writer(addStatisticsItemWriter())
			.build();
	}

	@Bean
	@StepScope
	public JpaCursorItemReader<BookingEntity> addStatisticsItemReader(@Value("#{jobParameters[from]}") String fromString
		, @Value("#{jobParameters[to]}") String toString) {
		final LocalDateTime from = LocalDateTime.parse(fromString);
		final LocalDateTime to = LocalDateTime.parse(toString);

		return new JpaCursorItemReaderBuilder<BookingEntity>()
			.name("addStatisticsItemReader")
			.entityManagerFactory(entityManagerFactory)
			.queryString("select b from BookingEntity b where b.endedAt between :from and :to")
			.parameterValues(Map.of("from", from, "to", to))
			.build();
	}

	@Bean
	public ItemWriter<BookingEntity> addStatisticsItemWriter() {
		return bookingEntities -> {
			Map<LocalDateTime, StatisticsEntity> statisticsEntityMap = new LinkedHashMap<>();
			for (BookingEntity bookingEntity : bookingEntities) {
				final LocalDateTime statisticsDate = bookingEntity.getStatisticsAt();
				StatisticsEntity statisticsEntity = statisticsEntityMap.get(statisticsDate);
				if (statisticsEntity == null) {
					statisticsEntityMap.put(statisticsDate, StatisticsEntity.create(bookingEntity));
				} else {
					statisticsEntity.add(bookingEntity);
				}
			}
			final List<StatisticsEntity> statisticsEntities = new ArrayList<>(statisticsEntityMap.values());
			statisticsRepository.saveAll(statisticsEntities);
		};
	}

	@Bean
	public Step makeDailyStatisticsStep() {
		return stepBuilderFactory.get("makeDailyStatisticsStep")
			.tasklet(makeDailyStatisticsTasklet)
			.build();
	}

	@Bean
	public Step makeWeeklyStatisticsStep() {
		return stepBuilderFactory.get("makeWeeklyStatisticsStep")
			.tasklet(makeWeeklyStatisticsTasklet)
			.build();
	}
}
