package me.mocadev.springbatch.pass.repository.user;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGroupMappingRepository extends JpaRepository<UserGroupMappingEntity, UserGroupMappingId> {

	List<UserGroupMappingEntity> findByUserGroupId(String userGroupId);
}
