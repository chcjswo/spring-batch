package me.mocadev.springbatch.pass.repository.packaze;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageRepository extends JpaRepository<PackageEntity, Integer> {

//	List<PackageEntity> findByCreatedAtAfter(LocalDateTime dateTime, Pageable pageable);
}
