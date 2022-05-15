package me.mocadev.springbatch.part5;

import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-15
 **/
@Entity
@Getter
@NoArgsConstructor
public class Orders {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String itemName;

	private int amount;

	private LocalDate createdDate;

	@Builder
	private Orders(String itemName, int amount, LocalDate createdDate) {
		this.itemName = itemName;
		this.amount = amount;
		this.createdDate = createdDate;
	}
}
