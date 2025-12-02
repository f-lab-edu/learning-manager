package me.chan99k.learningmanager.auth;

import java.time.Instant;
import java.util.List;

public interface JwtProvider {

	String createAccessToken(Long memberId, String email, List<String> roles);

	/**
	 * 토큰을 검증하고 클레임을 추출
	 *
	 * @param token JWT 문자열
	 * @return 클레임 정보
	 * @throws me.chan99k.learningmanager.exception.DomainException 토큰이 유효하지 않은 경우
	 */
	Claims validateAndGetClaims(String token);

	/**
	 * 토큰 유효성만 검사 (예외 발생 X)
	 */
	boolean isValid(String token);

	long getAccessTokenExpirationSeconds();

	record Claims(
		Long memberId,
		String email,
		List<String> roles,
		Instant expiresAt
	) {
	}
}
