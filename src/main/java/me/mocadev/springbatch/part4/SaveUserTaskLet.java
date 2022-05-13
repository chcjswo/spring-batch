package me.mocadev.springbatch.part4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-14
 **/
@Slf4j
@RequiredArgsConstructor
public class SaveUserTaskLet implements Tasklet {

	private final UserRepository userRepository;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
		throws Exception {
		List<User> users = createUsers();

		Collections.shuffle(users);
		userRepository.saveAll(users);

		return RepeatStatus.FINISHED;
	}

	private List<User> createUsers() {
		List<User> users = new ArrayList<>();
		userIteration(0, 100, users, 1_000);
		userIteration(100, 200, users, 200_000);
		userIteration(200, 300, users, 300_000);
		userIteration(300, 400, users, 500_000);
		return users;
	}

	private void userIteration(int index, int length, List<User> users, int totalAmount) {
		for (int i = index; i < length; i++) {
			makeUsers(users, totalAmount, i);
		}
	}

	private void makeUsers(List<User> users, int totalAmount, int i) {
		users.add(User.builder()
			.totalAmount(totalAmount)
			.username("test username " + i)
			.build());
	}
}
