package me.mocadev.springbatch.part4;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import me.mocadev.springbatch.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
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
 * @since 2022-05-14
 **/
@SpringBatchTest
@ContextConfiguration(classes = {UserConfiguration.class, TestConfiguration.class})
class UserConfigurationTest {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private UserRepository userRepository;

	@Test
	void test1() throws Exception {
		final JobExecution jobExecution = jobLauncherTestUtils.launchJob();

		int size = userRepository.findAllByUpdatedDate(LocalDate.now()).size();

		assertThat(jobExecution.getStepExecutions().stream()
			.filter(x -> x.getStepName().equals("userLevelUpStep"))
			.mapToInt(StepExecution::getWriteCount)
			.sum())
			.isEqualTo(size)
			.isEqualTo(300);

		assertThat(userRepository.count())
			.isEqualTo(400);
	}

}
