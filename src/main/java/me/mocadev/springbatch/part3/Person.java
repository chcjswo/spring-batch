package me.mocadev.springbatch.part3;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-04
 **/
@Entity
@Getter
@NoArgsConstructor
public class Person {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String name;
	private String age;
	private String address;

	public Person(String name, String age, String address) {
		this(0, name, age, address);
	}

	public Person(int id, String name, String age, String address) {
		this.id = id;
		this.name = name;
		this.age = age;
		this.address = address;
	}

	public boolean isNotEmptyName() {
		return Objects.nonNull(this.name) && !name.isEmpty();
	}

	public Person unknownName() {
		this.name = "unknown";
		return this;
	}
}
