package me.mocadev.springbatch.pass.job.pass;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mocadev.springbatch.pass.repository.pass.BulkPassEntity;
import me.mocadev.springbatch.pass.repository.pass.BulkPassRepository;
import me.mocadev.springbatch.pass.repository.pass.BulkPassStatus;
import me.mocadev.springbatch.pass.repository.pass.PassEntity;
import me.mocadev.springbatch.pass.repository.pass.PassModelMapper;
import me.mocadev.springbatch.pass.repository.pass.PassRepository;
import me.mocadev.springbatch.pass.repository.user.UserGroupMappingEntity;
import me.mocadev.springbatch.pass.repository.user.UserGroupMappingRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AddPassesTasklet implements Tasklet {

	private final PassRepository passRepository;
	private final BulkPassRepository bulkPassRepository;
	private final UserGroupMappingRepository userGroupMappingRepository;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		// 이용권 시작 일시 1일 전 user group 내 각 사용자에게 이용권을 추가한다.
		final LocalDateTime startedAt = LocalDateTime.now().minusDays(1);
		final List<BulkPassEntity> bulkPassEntities = bulkPassRepository.findByStatusAndStartedAtGreaterThan(BulkPassStatus.READY, startedAt);

		int count = 0;
		// 대량 이용권 정보를 돌면서 user group에 속한 userId를 조회하고 해당 userId로 추가한다.
		for (BulkPassEntity bulkPassEntity : bulkPassEntities) {
			final List<String> userIds = userGroupMappingRepository.findByUserGroupId(bulkPassEntity.getUserGroupId())
				.stream()
				.map(UserGroupMappingEntity::getUserId)
				.collect(Collectors.toList());
			count += addPasses(bulkPassEntity, userIds);
			bulkPassEntity.setStatus(BulkPassStatus.COMPLETED);
		}

		log.info("addPassesTasklet - execute: 이용권 {}건 추가 완료, startedAt: {}", count, startedAt);
		return RepeatStatus.FINISHED;
	}

	private int addPasses(BulkPassEntity bulkPassEntity, List<String> userIds) {
		List<PassEntity> passEntities = new ArrayList<>();
		for (String userId : userIds) {
			PassEntity passEntity = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity, userId);
			passEntities.add(passEntity);
		}
		return passRepository.saveAll(passEntities).size();
	}
}
