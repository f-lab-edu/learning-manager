package me.chan99k.learningmanager.domain;

import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

/**
 * 생성/수정 시간 및 생성/수정 주체를 관리하기 위한 엔티티.
 * <p>
 * Instant 타입을 사용하여 타임존에 독립적으로 동작하도록 한다.
 * <p>
 * 버전 정보를 관리하여 낙관적 잠금을 지원한다.
 */

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
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

	@Version
	private Long version;

	/**
	 * 엔티티의 고유 식별자를 반환한다.
	 * 다만, 엔티티가 아직 영속화 되지 않았다면 null 을 반환할 수도 있다.
	 * <p>
	 * 1. Transient 상태: null 반환
	 * <p>
	 * - new Member() 와 같이 객체가 처음 생성되고 아직 데이터베이스에 저장되지 않은 경우
	 * <p>
	 * - 데이터베이스로 부터 ID 를 할당받는 전략을 사용하고 있는데, ID 를 아직 할당 받지 못하였으므로 id = null 을 반환.
	 * <p>
	 * 2. Persistent 상태: null 이 아닌 id 반환
	 * <p>
	 * - 데이터베이스에 저장된 이후의 상태
	 * <p>
	 * - DB 가 생성한 ID 값을 가지고 있으므로, id 값은 null 이 아니다.
	 * @return Long id 또는 null
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

	public Long getVersion() {
		return version;
	}
}
