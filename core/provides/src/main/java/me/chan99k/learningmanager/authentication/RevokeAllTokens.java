package me.chan99k.learningmanager.authentication;

public interface RevokeAllTokens {

	/**
	 * 요청한 회원의 모든 Refresh Token을 폐기합니다.
	 *
	 * @param memberId 로그아웃할 회원 ID
	 */
	void revokeAll(Long memberId);
}
