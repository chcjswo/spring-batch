package me.mocadev.springbatch.part3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.springframework.batch.item.ItemProcessor;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-12
 **/
public class DuplicateValidationProcessor<T> implements ItemProcessor<T, T> {

	private final Map<String, Object> keyPool = new ConcurrentHashMap<>();
	private final Function<T, String> keyExtractor;
	private final boolean allowDuplicate;

	public DuplicateValidationProcessor(Function<T, String> keyExtractor, boolean allowDuplicate) {
		this.keyExtractor = keyExtractor;
		this.allowDuplicate = allowDuplicate;
	}

	@Override
	public T process(T item) {
		if (allowDuplicate) {
			return item;
		}
		final String key = keyExtractor.apply(item);
		if (keyPool.containsKey(key)) {
			return null;
		}
		keyPool.put(key, key);
		return item;
	}
}
