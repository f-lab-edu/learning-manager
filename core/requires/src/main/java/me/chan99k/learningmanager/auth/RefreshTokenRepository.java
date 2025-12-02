package me.chan99k.learningmanager.auth;

import java.util.Optional;

public interface RefreshTokenRepository {

	RefreshToken save(RefreshToken refreshToken);

	Optional<RefreshToken> findByToken(String token);

	void revokeAllByMemberId(Long memberId);

	void revokeByToken(String token);

	int deleteExpiredTokens();
}
