package me.chan99k.learningmanager.domain;

import java.time.Instant;

/**
 * 생성/수정 시간 및 생성/수정 주체를 관리하기 위한 엔티티.
 * <p>
 * Instant 타입을 사용하여 타임존에 독립적으로 동작하도록 한다.
 * <p>
 * 버전 정보를 관리하여 낙관적 잠금을 지원한다.
 */
public abstract class AbstractEntity {
	private Long id;
	private Instant createdAt;
	private Long createdBy;
	private Instant lastModifiedAt;
	private Long lastModifiedBy;
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
	public Long getId() {
		return id;
	}

	protected void setId(Long id) {
		this.id = id;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	protected void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	protected void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Instant getLastModifiedAt() {
		return lastModifiedAt;
	}

	protected void setLastModifiedAt(Instant lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt;
	}

	public Long getLastModifiedBy() {
		return lastModifiedBy;
	}

	protected void setLastModifiedBy(Long lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Long getVersion() {
		return version;
	}

	protected void setVersion(Long version) {
		this.version = version;
	}
}
