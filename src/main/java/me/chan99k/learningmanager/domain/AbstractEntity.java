package me.chan99k.learningmanager.domain;

import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * 생성/수정 시간 및 생성/수정 주체를 관리하기 위한 엔티티.
 * Instant 타입을 사용하여 타임존에 독립적으로 동작하도록 한다.
 */
@Getter
@MappedSuperclass
public abstract class AbstractEntity {
	@Id
	@Getter(onMethod_ = {@Nullable})
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@LastModifiedDate
	private Instant lastModifiedAt;

	@CreatedBy
	@Column(nullable = false, updatable = false)
	private Long createdBy;

	@LastModifiedBy
	private Long lastModifiedBy;
}
