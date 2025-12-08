package me.chan99k.learningmanager.attendance;

import java.util.List;
import java.util.Optional;

public interface AttendanceQueryRepository {

	/**
	 * 단순 조회
	 */
	Optional<Attendance> findBySessionIdAndMemberId(Long sessionId, Long memberId);

	/**
	 * 본인 출석 + 통계
	 */
	MemberAttendanceResult findMemberAttendanceWithStats(
		Long memberId, List<Long> sessionIds
	);

	/**
	 * 여러 멤버 출석 + 통계 (과정 매니저용)
	 */
	List<MemberAttendanceResult> findAllMembersAttendanceWithStats(
		List<Long> sessionIds, List<Long> memberIds
	);

	// === Records ===

	record MemberAttendanceResult(
		Long memberId,
		List<AttendanceRecord> attendances,
		AttendanceStats stats
	) {}

	record AttendanceRecord(
		String attendanceId,
		Long sessionId,
		AttendanceStatus finalStatus
	) {
	}

	record AttendanceStats(
		int total, int present, int absent,
		int late, int leftEarly, double rate
	) {}
}
