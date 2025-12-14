package me.chan99k.learningmanager.authentication;

import java.time.Instant;

public interface JwtProvider {

	/**
	 * Access Token 생성 - 신원 정보만 포함, 역할은 런타임에 조회
	 */
	String createAccessToken(Long memberId, String email);

	/**
	 * 토큰을 검증하고 클레임을 추출
	 *
	 * @param token JWT 문자열
	 * @return 클레임 정보
	 */
	Claims validateAndGetClaims(String token);

	/**
	 * 토큰 유효성만 검사 - 예외 발생 X
	 */
	boolean isValid(String token);

	long getAccessTokenExpirationSeconds();

	record Claims(
		Long memberId,
		String email,
		Instant expiresAt
	) {
	}

}
