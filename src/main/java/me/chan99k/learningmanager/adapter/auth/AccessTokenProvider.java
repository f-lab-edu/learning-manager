package me.chan99k.learningmanager.adapter.auth;

/**
 * @param <ID> JWT: Long memberId → Subject에 문자열로 저장 / Opaque Token: UUID sessionId → 데이터베이스 키 / Custom Token: String email → 이메일 기반 토큰 등
 */
public interface AccessTokenProvider<ID> {
	/**
	 * ID로 Access Token 생성
	 */
	String createAccessToken(ID id);

	/**
	 * Access Token 유효성 검증
	 */
	boolean validateAccessToken(String accessToken);

	/**
	 * Access Token에서 ID 추출
	 */
	ID getIdFromAccessToken(String accessToken);

	/**
	 * Access Token에서 ID 추출 (String)
	 */
	String getIdAsString(String accessToken);
}
