package me.mocadev.springbatch.part3;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-12
 **/
public interface PersonRepository extends JpaRepository<Person, Integer> {

}
