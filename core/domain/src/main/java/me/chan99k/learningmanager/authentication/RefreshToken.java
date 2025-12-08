package me.chan99k.learningmanager.authentication;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

// TODO :: 편의상 도메인 계층에 두었지만, 사실 도메인은 이런 토큰의 존재조차 몰라야 하는 것이 맞다. 어댑터 혹은 별도의 인증 서버로 분리가 필요함
public class RefreshToken {

	private Long id;
	private String token;
	private Long memberId;
	private Instant expiresAt;
	private Instant createdAt;
	private boolean revoked;

	protected RefreshToken() {
	}

	public RefreshToken(Long id, String token, Long memberId,
		Instant expiresAt, Instant createdAt, boolean revoked) {
		this.id = id;
		this.token = token;
		this.memberId = memberId;
		this.expiresAt = expiresAt;
		this.createdAt = createdAt;
		this.revoked = revoked;
	}

	public static RefreshToken create(Long memberId, Duration ttl) {
		return new RefreshToken(
			null,
			UUID.randomUUID().toString(),
			memberId,
			Instant.now().plus(ttl),
			Instant.now(),
			false
		);
	}

	public boolean isExpired() {
		return Instant.now().isAfter(expiresAt);
	}

	public boolean isUsable() {
		return !revoked && !isExpired();
	}

	public void revoke() {
		this.revoked = true;
	}

	// === Getter ===

	public Long getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public Long getMemberId() {
		return memberId;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public boolean isRevoked() {
		return revoked;
	}
}