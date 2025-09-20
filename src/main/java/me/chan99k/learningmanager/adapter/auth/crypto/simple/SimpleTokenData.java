package me.chan99k.learningmanager.adapter.auth.crypto.simple;

import java.time.Instant;

public class SimpleTokenData {

	private final String subject;
	private final Long memberId;
	private final String value;     // 토큰 값 또는 추가 데이터
	private final Instant expiresAt;

	public SimpleTokenData(String subject, Long memberId, String value, Instant expiresAt) {
		this.subject = subject;
		this.memberId = memberId;
		this.value = value;
		this.expiresAt = expiresAt;
	}

	public String getSubject() {
		return subject;
	}

	public Long getMemberId() {
		return memberId;
	}

	public String getValue() {
		return value;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public boolean isExpired() {
		return expiresAt != null && Instant.now().isAfter(expiresAt);
	}
}