package me.mocadev.springbatch.part3;


import java.util.ArrayList;
import java.util.List;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-04
 **/
public class CustomItemReader<T> implements ItemReader<T> {

	private final List<T> items;

	public CustomItemReader(List<T> items) {
		this.items = new ArrayList<>(items);
	}

	@Override
	public T read()
		throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if (!items.isEmpty()) {
			return items.remove(0);
		}
		return null;
	}
}
