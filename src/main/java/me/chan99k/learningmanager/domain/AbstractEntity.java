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

/**
 * 생성/수정 시간 및 생성/수정 주체를 관리하기 위한 엔티티.
 * Instant 타입을 사용하여 타임존에 독립적으로 동작하도록 한다.
 */

@MappedSuperclass
public abstract class AbstractEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@CreatedBy
	@Column(nullable = false, updatable = false)
	private Long createdBy;

	@LastModifiedDate
	private Instant lastModifiedAt;

	@LastModifiedBy
	private Long lastModifiedBy;

	/**
	 * 엔티티의 고유 식별자를 반환합니다.
	 * 다만, 엔티티가 아직 영속화 되지 않았다면 null 을 반환할 수도 있습니다.
	 * @return ID 또는 null
	 */
	@Nullable
	public Long getId() {
		return id;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getLastModifiedAt() {
		return lastModifiedAt;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public Long getLastModifiedBy() {
		return lastModifiedBy;
	}
}
