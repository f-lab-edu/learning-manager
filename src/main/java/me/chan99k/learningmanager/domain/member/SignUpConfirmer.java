package me.chan99k.learningmanager.domain.member;

import java.time.Duration;

public interface SignUpConfirmer {
	String generateAndStoreToken(Long memberId, String email, Duration expiration);

	boolean validateToken(String token);

	Long getMemberIdByToken(String token);

	void removeToken(String token);
}
