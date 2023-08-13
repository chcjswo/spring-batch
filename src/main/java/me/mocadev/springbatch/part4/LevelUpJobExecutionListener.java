package me.mocadev.springbatch.part4;

import java.time.LocalDate;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-15
 **/
@Slf4j
@RequiredArgsConstructor
public class LevelUpJobExecutionListener implements JobExecutionListener {

	private final UsersRepository usersRepository;

	@Override
	public void beforeJob(JobExecution jobExecution) {

	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		final Collection<User> users = usersRepository.findAllByUpdatedDate(LocalDate.now());

		final long time = jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime();
		log.info("회원 등급 업데이트 배치 프로그램");
		log.info("----------------------------------");
		log.info("총 데이터 처리 {}건, 처리 시간 {}millis", users.size(), time);
	}
}
