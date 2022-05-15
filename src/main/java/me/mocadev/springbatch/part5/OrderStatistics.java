package me.mocadev.springbatch.part5;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-15
 **/
@Getter
public class OrderStatistics {

	private final String amount;
	private final LocalDate date;

	@Builder
	private OrderStatistics(String amount, LocalDate date) {
		this.amount = amount;
		this.date = date;
	}
}
