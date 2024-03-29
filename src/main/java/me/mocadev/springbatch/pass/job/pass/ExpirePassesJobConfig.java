package me.mocadev.springbatch.pass.job.pass;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import me.mocadev.springbatch.pass.repository.pass.PassEntity;
import me.mocadev.springbatch.pass.repository.pass.PassStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class ExpirePassesJobConfig {

	private static final int CHUNK_SIZE = 5;

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;

	@Bean
	public Job expirePassesJob() {
		return this.jobBuilderFactory.get("expirePassesJob")
			.start(this.expirePassesStep())
			.build();
	}

	@Bean
	public Step expirePassesStep() {
		return this.stepBuilderFactory.get("expirePassesStep")
			.<PassEntity, PassEntity>chunk(CHUNK_SIZE)
			.reader(expirePassesItemReader())
			.processor(expirePassesItemProcessor())
			.writer(expirePassesItemWriter())
			.build();
	}

	@Bean
	@StepScope
	public JpaCursorItemReader<PassEntity> expirePassesItemReader() {
		return new JpaCursorItemReaderBuilder<PassEntity>()
			.name("expirePassesItemReader")
			.entityManagerFactory(entityManagerFactory)
			.queryString("select p from PassEntity p where p.status = :status and p.endedAt <= :endedAt")
			.parameterValues(Map.of("status", PassStatus.PROGRESSED, "endedAt", LocalDateTime.now()))
			.build();
	}

	@Bean
	public ItemProcessor<PassEntity, PassEntity> expirePassesItemProcessor() {
		return passEntity -> {
			passEntity.setStatus(PassStatus.EXPIRED);
			passEntity.setExpiredAt(LocalDateTime.now());
			return passEntity;
		};
	}

	@Bean
	public JpaItemWriter<PassEntity> expirePassesItemWriter() {
		return new JpaItemWriterBuilder<PassEntity>()
			.entityManagerFactory(entityManagerFactory)
			.build();
	}
}
