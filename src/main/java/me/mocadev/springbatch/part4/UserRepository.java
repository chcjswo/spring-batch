package me.mocadev.springbatch.part4;

import java.time.LocalDate;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-14
 **/
public interface UserRepository extends JpaRepository<User, Long> {

	Collection<User> findAllByUpdatedDate(LocalDate updatedDate);

	@Query(value = "select min(u.id) from User u")
	long findMinId();

	@Query(value = "select max(u.id) from User u")
	long findMaxId();
}
