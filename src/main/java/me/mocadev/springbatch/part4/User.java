package me.mocadev.springbatch.part4;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mocadev.springbatch.part5.Orders;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-14
 **/
@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String username;

	@Enumerated(EnumType.STRING)
	private Level level = Level.NORMAL;

	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	private List<Orders> orders;

	private LocalDate updatedDate;

	@Builder
	private User(String username, List<Orders> orders) {
		this.username = username;
		this.orders = orders;
	}

	public boolean availableLevelUp() {
		return Level.availableLevelUp(this.getLevel(), this.getTotalAmount());
	}

	private int getTotalAmount() {
		return this.orders.stream()
			.mapToInt(Orders::getAmount)
			.sum();
	}

	public Level levelUp() {
		Level nextLevel = Level.getNextLevel(this.getTotalAmount());
		this.level = nextLevel;
		this.updatedDate = LocalDate.now();
		return nextLevel;
	}
}
