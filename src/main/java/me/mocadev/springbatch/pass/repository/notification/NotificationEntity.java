package me.mocadev.springbatch.pass.repository.notification;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.mocadev.springbatch.pass.repository.BaseEntity;

@Getter
@Setter
@ToString
@Entity
@Table(name = "notification")
public class NotificationEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임합니다. (AUTO_INCREMENT)
	private Integer notificationSeq;
	private String uuid;

	private NotificationEvent event;
	private String text;
	private boolean sent;
	private LocalDateTime sentAt;

}
