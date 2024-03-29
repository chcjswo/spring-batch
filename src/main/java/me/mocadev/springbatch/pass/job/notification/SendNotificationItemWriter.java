package me.mocadev.springbatch.pass.job.notification;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mocadev.springbatch.pass.addapter.message.KakaoTalkMessageAdapter;
import me.mocadev.springbatch.pass.repository.notification.NotificationEntity;
import me.mocadev.springbatch.pass.repository.notification.NotificationRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SendNotificationItemWriter implements ItemWriter<NotificationEntity> {

	private final NotificationRepository notificationRepository;
	private final KakaoTalkMessageAdapter kakaoTalkMessageAdapter;

	@Override
	public void write(List<? extends NotificationEntity> notificationEntities) {
		int count = 0;

		for (NotificationEntity notificationEntity : notificationEntities) {
			boolean successful = kakaoTalkMessageAdapter.sendKakaoTalkMessage(notificationEntity.getUuid(), notificationEntity.getText());

			if (successful) {
				notificationEntity.setSent(true);
				notificationEntity.setSentAt(LocalDateTime.now());
				notificationRepository.save(notificationEntity);
				count++;
			}
		}
		log.info("SendNotificationItemWriter - write: 수업 전 알람 {}/{}건 전송 성공", count, notificationEntities.size());
	}
}
