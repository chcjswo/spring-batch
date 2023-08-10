package me.mocadev.springbatch.pass.job.notification;

import javax.persistence.EntityManagerFactory;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import me.mocadev.springbatch.pass.repository.booking.BookingEntity;
import me.mocadev.springbatch.pass.repository.notification.NotificationEntity;
import me.mocadev.springbatch.pass.repository.notification.NotificationEvent;
import me.mocadev.springbatch.pass.repository.notification.NotificationModelMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@RequiredArgsConstructor
@Configuration
public class SendNotificationBeforeClassJobConfig {

	private static final int CHUNK_SIZE = 10;

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;
	private final SendNotificationItemWriter sendNotificationItemWriter;

	@Bean
	public Job sendNotificationBeforeClassJob() {
		return jobBuilderFactory.get("sendNotificationBeforeClassJob")
			.start(addNotificationStep())
			.next(sendNotificationStep())
			.build();
	}

	@Bean
	public Step addNotificationStep() {
		return stepBuilderFactory.get("addNotificationStep")
			.<BookingEntity, NotificationEntity>chunk(CHUNK_SIZE)
			.reader(addNotificationItemReader())
			.processor(addNotificationItemProcessor())
			.writer(addNotificationItemWriter())
			.build();
	}

	@Bean
	public JpaPagingItemReader<BookingEntity> addNotificationItemReader() {
		return new JpaPagingItemReaderBuilder<BookingEntity>()
			.name("addNotificationItemReader")
			.entityManagerFactory(entityManagerFactory)
			.pageSize(CHUNK_SIZE)
			.queryString("SELECT b FROM BookingEntity b join fetch b.userEntity " +
				"WHERE b.status = :status and b.startedAt <= :statedAt " +
				"order by b.bookingSeq")
			.build();
	}

	@Bean
	public ItemProcessor<BookingEntity, NotificationEntity> addNotificationItemProcessor() {
		return bookingEntity -> NotificationModelMapper.INSTANCE.toNotificationEntity(bookingEntity, NotificationEvent.BEFORE_CLASS);
	}

	@Bean
	public JpaItemWriter<NotificationEntity> addNotificationItemWriter() {
		return new JpaItemWriterBuilder<NotificationEntity>()
			.entityManagerFactory(entityManagerFactory)
			.build();
	}

	@Bean
	public Step sendNotificationStep() {
		return stepBuilderFactory.get("sendNotificationStep")
			.<NotificationEntity, NotificationEntity>chunk(CHUNK_SIZE)
			.reader(sendNotificationReader())
			.writer(sendNotificationItemWriter)
			.taskExecutor(new SimpleAsyncTaskExecutor())
			.build();
	}

	@Bean
	public SynchronizedItemStreamReader<NotificationEntity> sendNotificationReader() {
		JpaCursorItemReader<NotificationEntity> itemReader = new JpaCursorItemReaderBuilder<NotificationEntity>()
			.name("sendNotificationReader")
			.entityManagerFactory(entityManagerFactory)
			.queryString("SELECT n FROM NotificationEntity n WHERE n.event = :event and n.sent = :sent")
			.parameterValues(Map.of("event", NotificationEvent.BEFORE_CLASS, "sent", false))
			.build();

		return new SynchronizedItemStreamReaderBuilder<NotificationEntity>()
			.delegate(itemReader)
			.build();
	}
}
