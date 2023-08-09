package me.mocadev.springbatch.pass.repository.pass;

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

@Getter
@Setter
@ToString
@Entity
@Table(name = "bulk_pass")
public class BulkPassEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임합니다. (AUTO_INCREMENT)
	private Integer bulkPassSeq;
	private Integer packageSeq;
	private String userGroupId;

	@Enumerated(EnumType.STRING)
	private BulkPassStatus status;
	private Integer count;

	private LocalDateTime startedAt;
	private LocalDateTime endedAt;

}
