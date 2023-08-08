package me.mocadev.springbatch.repository.packaze;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageRepository extends JpaRepository<PackageEntity, Integer> {

	List<PackageEntity> findByCreatedAtAfter(LocalDateTime dateTime, Pageable pageable);
}
