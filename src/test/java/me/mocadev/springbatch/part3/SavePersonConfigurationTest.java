package me.mocadev.springbatch.part3;

import me.mocadev.springbatch.config.TestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-12
 **/
@Disabled
@SpringBatchTest
@ContextConfiguration(classes = {SavePersonConfiguration.class, TestConfiguration.class})
class SavePersonConfigurationTest {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private PersonRepository personRepository;

	@AfterEach
	public void afterEach() {
		personRepository.deleteAll();
	}

	@Test
	void test_step() {
		final JobExecution jobExecution = jobLauncherTestUtils.launchStep("savePersonStep");

		Assertions.assertThat(jobExecution.getStepExecutions().stream()
			.mapToInt(StepExecution::getWriteCount)
			.sum())
			.isEqualTo(personRepository.count())
			.isEqualTo(5);
	}

	@Test
	void test_allow_duplicate() throws Exception {
		// given
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("allow_duplicate", "false")
			.toJobParameters();

		// when
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

		// then
		Assertions.assertThat(jobExecution.getStepExecutions().stream()
			.mapToInt(StepExecution::getWriteCount)
			.sum())
			.isEqualTo(personRepository.count())
			.isEqualTo(5);
	}

	@Test
	void test_not_allow_duplicate() throws Exception {
		// given
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("allow_duplicate", "true")
			.toJobParameters();

		// when
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

		// then
		Assertions.assertThat(jobExecution.getStepExecutions().stream()
			.mapToInt(StepExecution::getWriteCount)
			.sum())
			.isEqualTo(personRepository.count())
			.isEqualTo(100);
	}

}
