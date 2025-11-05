package me.chan99k.learningmanager.adapter.auth.crypto.simple;

import java.time.Instant;

/**
 * @param value  토큰 값 또는 추가 데이터 */
public record SimpleTokenData(String subject, Long memberId, String value, Instant expiresAt) {

	public boolean isExpired() {
		return expiresAt != null && Instant.now().isAfter(expiresAt);
	}
}