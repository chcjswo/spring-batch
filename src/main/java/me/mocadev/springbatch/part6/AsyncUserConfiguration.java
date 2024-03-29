package me.mocadev.springbatch.part6;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mocadev.springbatch.part4.LevelUpJobExecutionListener;
import me.mocadev.springbatch.part4.SaveUserTaskLet;
import me.mocadev.springbatch.part4.User;
import me.mocadev.springbatch.part4.UsersRepository;
import me.mocadev.springbatch.part5.JobParametersDecide;
import me.mocadev.springbatch.part5.OrderStatistics;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-14(014)
 **/
@Configuration
@Slf4j
@RequiredArgsConstructor
public class AsyncUserConfiguration {

	private final String JOB_NAME = "asyncUserJob";
	private final int CHUNK_SIZE = 1000;
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final UsersRepository usersRepository;
	private final EntityManagerFactory entityManagerFactory;
	private final DataSource dataSource;
	private final TaskExecutor taskExecutor;

	@Bean(JOB_NAME)
	public Job userJob() throws Exception {
		return jobBuilderFactory.get(JOB_NAME)
			.incrementer(new RunIdIncrementer())
			.start(this.saveUserStep())
			.next(this.userLevelUpStep())
			.listener(new LevelUpJobExecutionListener(usersRepository))
			.next(new JobParametersDecide("date"))
			.on(JobParametersDecide.CONTINUE.getName())
			.to(this.orderStatisticsStep(null))
			.build()
			.build();
	}

	@Bean(JOB_NAME + "_saveUserStep")
	public Step saveUserStep() {
		return stepBuilderFactory.get(JOB_NAME + "_saveUserStep")
			.tasklet(new SaveUserTaskLet(usersRepository))
			.build();
	}

	@Bean(JOB_NAME + "_userLevelUp")
	public Step userLevelUpStep() throws Exception {
		return stepBuilderFactory.get(JOB_NAME + "_userLevelUp")
			.<User, Future<User>>chunk(CHUNK_SIZE)
			.reader(itemReader())
			.processor(itemProcessor())
			.writer(itemWriter())
			.build();
	}

	@Bean(JOB_NAME + "_orderStatisticsStep")
	@JobScope
	public Step orderStatisticsStep(@Value("#{jobParameters[date]}") String date) throws Exception {
		return this.stepBuilderFactory.get(JOB_NAME + "_orderStatisticsStep")
			.<OrderStatistics, OrderStatistics>chunk(100)
			.reader(orderStatisticsItemReader(date))
			.writer(orderStatisticsItemWriter(date))
			.build();
	}

	private ItemWriter<? super OrderStatistics> orderStatisticsItemWriter(String date)
		throws Exception {
		YearMonth yearMonth = YearMonth.parse(date);
		String fileName = yearMonth.getYear() + "년_" + yearMonth.getMonthValue() + "월_일별_주문_금액.csv";

		final BeanWrapperFieldExtractor<OrderStatistics> fieldExtractor = new BeanWrapperFieldExtractor<>();
		fieldExtractor.setNames(new String[] {"amount", "date"});

		final DelimitedLineAggregator<OrderStatistics> lineAggregator = new DelimitedLineAggregator<>();
		lineAggregator.setDelimiter(",");
		lineAggregator.setFieldExtractor(fieldExtractor);

		final FlatFileItemWriter<OrderStatistics> itemWriter = new FlatFileItemWriterBuilder<OrderStatistics>()
			.resource(new FileSystemResource("output/" + fileName))
			.lineAggregator(lineAggregator)
			.name("orderStatisticsItemWriter")
			.encoding("UTF-8")
			.headerCallback(writer -> writer.write("total_amount,date"))
			.build();
		itemWriter.afterPropertiesSet();
		return itemWriter;
	}

	private ItemReader<? extends OrderStatistics> orderStatisticsItemReader(String date)
		throws Exception {
		YearMonth yearMonth = YearMonth.parse(date);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("startDate", yearMonth.atDay(1));
		parameters.put("endDate", yearMonth.atEndOfMonth());

		Map<String, Order> sortKey = new HashMap<>();
		sortKey.put("created_date", Order.ASCENDING);

		final JdbcPagingItemReader<OrderStatistics> itemReader = new JdbcPagingItemReaderBuilder<OrderStatistics>()
			.dataSource(this.dataSource)
			.rowMapper((resultSet, i) -> OrderStatistics.builder()
				.amount(resultSet.getString(1))
				.date(LocalDate.parse(resultSet.getString(2), DateTimeFormatter.ISO_DATE))
				.build())
			.pageSize(CHUNK_SIZE)
			.name(JOB_NAME + "_orderStatisticsItemReader")
			.selectClause("sum(amount), created_date")
			.fromClause("orders")
			.whereClause("created_date >= :startDate and created_date <= :endDate")
			.groupClause("created_date")
			.parameterValues(parameters)
			.sortKeys(sortKey)
			.build();
		itemReader.afterPropertiesSet();
		return itemReader;
	}

	private AsyncItemWriter<User> itemWriter() {
		ItemWriter<User> itemWriter = users -> users.forEach(x -> {
			x.levelUp();
			usersRepository.save(x);
		});

		final AsyncItemWriter<User> asyncItemWriter = new AsyncItemWriter<>();
		asyncItemWriter.setDelegate(itemWriter);
		return asyncItemWriter;
	}

	private AsyncItemProcessor<User, User> itemProcessor() {
		ItemProcessor<User, User> itemProcessor = user -> {
			if (user.availableLevelUp()) {
				return user;
			}
			return null;
		};
		AsyncItemProcessor<User, User> asyncItemProcessor = new AsyncItemProcessor<>();
		asyncItemProcessor.setDelegate(itemProcessor);
		asyncItemProcessor.setTaskExecutor(taskExecutor);

		return asyncItemProcessor;
	}

	private ItemReader<? extends User> itemReader() throws Exception {
		final JpaPagingItemReader<User> itemReader = new JpaPagingItemReaderBuilder<User>()
			.queryString("select u from User u")
			.entityManagerFactory(entityManagerFactory)
			.pageSize(CHUNK_SIZE)
			.name(JOB_NAME + "_userItemReader")
			.build();
		itemReader.afterPropertiesSet();
		return itemReader;
	}

}
