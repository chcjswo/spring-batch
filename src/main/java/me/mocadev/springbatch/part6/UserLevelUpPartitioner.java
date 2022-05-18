package me.mocadev.springbatch.part6;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import me.mocadev.springbatch.part4.UserRepository;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-18
 **/
@RequiredArgsConstructor
public class UserLevelUpPartitioner implements Partitioner {

	private final UserRepository userRepository;

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		long minId = userRepository.findMinId();
		long maxId = userRepository.findMaxId();
		long targetSize = (maxId - minId) / gridSize + 1;

		Map<String, ExecutionContext> result = new HashMap<>();

		long number = 0;
		long start = minId;
		long end = start + targetSize - 1;

		while (start <= maxId) {
			final ExecutionContext value = new ExecutionContext();
			result.put("partition" + number, value);

			if (end >= maxId) {
				end = maxId;
			}

			value.putLong("minId", start);
			value.putLong("maxId", end);

			start += targetSize;
			end += targetSize;
			number++;
		}
		return null;
	}
}
