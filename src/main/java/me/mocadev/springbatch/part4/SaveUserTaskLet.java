package me.mocadev.springbatch.part4;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mocadev.springbatch.part5.Orders;
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

	private final UsersRepository usersRepository;
	private final int SIZE = 10_000;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		List<User> users = createUsers();

		Collections.shuffle(users);
		usersRepository.saveAll(users);

		return RepeatStatus.FINISHED;
	}

	private List<User> createUsers() {
		List<User> users = new ArrayList<>();
		userIteration(0, SIZE, users, 1_000, LocalDate.of(2022, 5, 15));
		userIteration(SIZE, SIZE * 2, users, 200_000, LocalDate.of(2022, 5, 16));
		userIteration(SIZE * 2, SIZE * 3, users, 300_000, LocalDate.of(2022, 5, 17));
		userIteration(SIZE * 3, SIZE * 4, users, 500_000, LocalDate.of(2022, 5, 18));
		return users;
	}

	private void userIteration(int index, int length, List<User> users, int amount, LocalDate date) {
		for (int i = index; i < length; i++) {
			makeUsers(users, amount, i, date);
		}
	}

	private void makeUsers(List<User> users, int amount, int i, LocalDate date) {
		users.add(User.builder()
			.username("test username " + i)
			.orders(Collections.singletonList(Orders.builder()
				.amount(amount)
				.itemName("item name " + i)
				.createdDate(date)
				.build()))
			.build());
	}
}
