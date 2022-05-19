package me.mocadev.springbatch.part7;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-19
 **/
@Component
@Slf4j
public class ScheduleTask {

	// 매 1분 마다
	@Scheduled(cron = "0 */1 * * * ?")
	public void task1() {
		log.info("task1 time: {}", LocalDateTime.now());
	}

	// 매 10초 마다
	@Scheduled(cron = "*/10 * * * * ?")
	public void task2() {
		log.info("task2 time: {}", LocalDateTime.now());
	}

}
