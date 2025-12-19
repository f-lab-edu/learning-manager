package me.chan99k.learningmanager.authorization;

public interface SessionSecurity {

	/**
	 * 해당 회원이 세션의 매니저인지 확인합니다.
	 * - 단독 세션: OPERATOR 이상 시스템 권한
	 * - 과정 세션: 과정의 MANAGER 권한
	 */
	boolean isSessionManager(Long sessionId, Long memberId);

	boolean isSessionManagerOrMentor(Long sessionId, Long memberId);

	boolean isSessionMember(Long sessionId, Long memberId);

	/**
	 * 해당 회원이 세션 참여자를 관리할 수 있는지 확인합니다.
	 * - 단독 세션: OPERATOR 이상 시스템 권한
	 * - 과정 세션: MANAGER 권한 또는 세션 HOST
	 */
	boolean canManageSessionParticipants(Long sessionId, Long memberId);
}
