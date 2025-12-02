package me.chan99k.learningmanager.auth;

/**
 * 현재 인증된 사용자의 컨텍스트 정보를 제공하는 인터페이스.
 * 실제 구현은 인증 시스템에서 제공해야 함.
 */
public interface UserContext {

	/**
	 * 현재 인증된 사용자의 회원 ID를 반환합니다.
	 *
	 * @return 현재 사용자의 회원 ID
	 */
	Long getCurrentMemberId();
}
