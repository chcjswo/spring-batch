package me.mocadev.springbatch.part2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-01
 **/
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SharedConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job shareJob() {
		return jobBuilderFactory.get("shareJob")
			.incrementer(new RunIdIncrementer())
			.start(this.shareStep1())
			.next(this.shareStep2())
			.build();
	}

	private Step shareStep1() {
		return stepBuilderFactory.get("shareStep1")
			.tasklet((contribution, chunkContext) -> {
				final StepExecution stepExecution = contribution.getStepExecution();
				final ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
				stepExecutionContext.putString("stepKey", "step execution context");

				final JobExecution jobExecution = stepExecution.getJobExecution();
				final JobInstance jobInstance = jobExecution.getJobInstance();
				final ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();
				jobExecutionContext.putString("jobKey", "job execution context");
				final JobParameters jobParameters = jobExecution.getJobParameters();

				log.info("jobName: {}, stepName = {}, parameter = {}",
					jobInstance.getJobName(),
					stepExecution.getStepName(),
					jobParameters.getLong("run.id"));

				return RepeatStatus.FINISHED;
			}).build();
	}

	private Step shareStep2() {
		return stepBuilderFactory.get("shareStep2")
			.tasklet((contribution, chunkContext) -> {
				final StepExecution stepExecution = contribution.getStepExecution();
				final ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();

				final JobExecution jobExecution = stepExecution.getJobExecution();
				final ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();

				log.info("jobKey: {}, stepKey = {}",
					jobExecutionContext.getString("jobKey", "emptyJobKey"),
					stepExecutionContext.getString("stepKey", "emptyStepKey"));

				return RepeatStatus.FINISHED;
			}).build();
	}

}
