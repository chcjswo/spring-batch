package me.mocadev.springbatch.part4;

import java.time.LocalDate;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-14
 **/
public interface UserRepository extends JpaRepository<User, Long> {

	Collection<Object> findAllByUpdatedDate(LocalDate updatedDate);
}
