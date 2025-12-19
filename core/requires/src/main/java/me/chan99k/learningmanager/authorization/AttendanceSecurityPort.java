package me.chan99k.learningmanager.authorization;

public interface AttendanceSecurityPort {

	/**
	 * 해당 회원이 출석 수정을 요청할 수 있는지 확인합니다.
	 * - 시스템 역할: ADMIN, REGISTRAR
	 * - 과정 역할: MENTOR, MANAGER, LEAD_MANAGER
	 */
	boolean canRequestCorrection(String attendanceId, Long memberId);

	/**
	 * 해당 회원이 출석 수정을 승인할 수 있는지 확인합니다.
	 * - 시스템 역할: ADMIN, REGISTRAR
	 * - 과정 역할: 요청자보다 높은 권한 필요 (본인 요청 승인 불가)
	 * - LEAD_MANAGER: 모든 요청 승인 가능
	 */
	boolean canApproveCorrection(String attendanceId, Long memberId);
}
