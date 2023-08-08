package me.mocadev.springbatch.repository.pass;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.mocadev.springbatch.repository.BaseEntity;

@Getter
@Setter
@ToString
@Entity
@Table(name = "pass")
public class PassEntity extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임합니다. (AUTO_INCREMENT)
	private Integer passSeq;
	private Integer packageSeq;
	private String userId;

	@Enumerated(EnumType.STRING)
	private PassStatus status;
	private Integer remainingCount;

	private LocalDateTime startedAt;
	private LocalDateTime endedAt;
	private LocalDateTime expiredAt;

}
