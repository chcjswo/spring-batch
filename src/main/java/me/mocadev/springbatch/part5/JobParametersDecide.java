package me.mocadev.springbatch.part5;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-16
 **/
@RequiredArgsConstructor
public class JobParametersDecide implements JobExecutionDecider {

	public static final FlowExecutionStatus CONTINUE = new FlowExecutionStatus("CONTINUE");
	private final String key;

	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		final String value = jobExecution.getJobParameters().getString(key);
		if (StringUtils.isEmpty(value)) {
			return FlowExecutionStatus.COMPLETED;
		}
		return CONTINUE;
	}
}
