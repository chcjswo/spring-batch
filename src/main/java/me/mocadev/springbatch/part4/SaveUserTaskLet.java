package me.mocadev.springbatch.part4;

import java.time.LocalDate;
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
	private final int SIZE = 100;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
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

	private void userIteration(int index, int length, List<User> users, int amount) {
		for (int i = index; i < length; i++) {
			makeUsers(users, amount, i);
		}
	}

	private void makeUsers(List<User> users, int amount, int i) {
		users.add(User.builder()
			.username("test username " + i)
			.orders(Collections.singletonList(Orders.builder()
				.amount(amount)
				.itemName("item name " + i)
				.createdDate(LocalDate.of(2022, 5, 15))
				.build()))
			.build());
	}
}
