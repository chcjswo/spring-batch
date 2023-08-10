package me.mocadev.springbatch.pass.job.pass;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import me.mocadev.springbatch.pass.repository.booking.BookingEntity;
import me.mocadev.springbatch.pass.repository.booking.BookingRepository;
import me.mocadev.springbatch.pass.repository.booking.BookingStatus;
import me.mocadev.springbatch.pass.repository.pass.PassEntity;
import me.mocadev.springbatch.pass.repository.pass.PassRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@RequiredArgsConstructor
@Configuration
public class UsePassesJobConfig {

	private static final int CHUNK_SIZE = 10;

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;
	private final PassRepository passRepository;
	private final BookingRepository bookingRepository;

	@Bean
	public Job usePassesJob() {
		return jobBuilderFactory.get("usePassesJob")
			.start(usePassesStep())
			.build();
	}

	@Bean
	public Step usePassesStep() {
		return stepBuilderFactory.get("usePassesStep")
			.<BookingEntity, Future<BookingEntity>>chunk(CHUNK_SIZE)
			.reader(usePassesItemReader())
			.processor(usePassesAsyncItemProcessor())
			.writer(usePassesAsyncItemWriter())
			.build();
	}

	@Bean
	public JpaCursorItemReader<BookingEntity> usePassesItemReader() {
		return new JpaCursorItemReaderBuilder<BookingEntity>()
			.name("usePassesItemReader")
			.entityManagerFactory(entityManagerFactory)
			.queryString("SELECT b FROM BookingEntity b join fetch b.passEntity " +
				"WHERE b.status = :status and b.usePass = false and b.endedAt < :endedAt")
			.parameterValues(Map.of("status", BookingStatus.COMPLETED, "endedAt", LocalDateTime.now()))
			.build();
	}

	@Bean
	public AsyncItemProcessor<BookingEntity, BookingEntity> usePassesAsyncItemProcessor() {
		AsyncItemProcessor<BookingEntity, BookingEntity> asyncItemProcessor = new AsyncItemProcessor<>();
		asyncItemProcessor.setDelegate(usePassesItemProcessor());
		asyncItemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor());
		return asyncItemProcessor;
	}

	@Bean
	public ItemProcessor<BookingEntity, BookingEntity> usePassesItemProcessor() {
		return bookingEntity -> {
			PassEntity passEntity = bookingEntity.getPassEntity();
			passEntity.setRemainingCount(passEntity.getRemainingCount() - 1);
			bookingEntity.setPassEntity(passEntity);
			bookingEntity.setUsedPass(true);
			bookingRepository.save(bookingEntity);
			return bookingEntity;
		};
	}

	@Bean
	public AsyncItemWriter<BookingEntity> usePassesAsyncItemWriter() {
		AsyncItemWriter<BookingEntity> asyncItemWriter = new AsyncItemWriter<>();
		asyncItemWriter.setDelegate(usePassesItemWriter());
		return asyncItemWriter;
	}

	@Bean
	public ItemWriter<BookingEntity> usePassesItemWriter() {
		return bookingEntity -> {
			for (BookingEntity booking : bookingEntity) {
				int updatedCount = passRepository.updateRemainingCount(booking.getPassSeq(),
					booking.getPassEntity().getRemainingCount() - 1);
				if (updatedCount > 0) {
					bookingRepository.updateUsedPass(booking.getPassSeq(), booking.isUsedPass());
				}
			}
		};
	}
}
