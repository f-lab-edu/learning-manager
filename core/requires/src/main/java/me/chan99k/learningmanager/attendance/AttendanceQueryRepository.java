package me.chan99k.learningmanager.attendance;

import java.util.List;
import java.util.Optional;

public interface AttendanceQueryRepository {
	/**
	 * 특정 세션의 특정 멤버 출석 기록 조회
	 */
	Optional<Attendance> findBySessionIdAndMemberId(Long sessionId, Long memberId);

	/**
	 * 특정 회원의 모든 출석 기록 조회
	 */
	List<Attendance> findByMemberId(Long memberId);

	/**
	 * 특정 회원의 특정 세션들 출석 기록 조회
	 */
	List<Attendance> findByMemberIdAndSessionIds(Long memberId, List<Long> sessionIds);

	/**
	 * 특정 세션들의 출석 기록 조회
	 * 향후 MongoDB 통합 시 Aggregation으로 교체 예정
	 */
	List<AttendanceProjection> findAttendanceProjectionByMemberIdAndSessionIds(
		Long memberId,
		List<Long> sessionIds
	);

	/**
	 * 출석 기록 프로젝션 (필요한 필드만)
	 */
	record AttendanceProjection(
		String attendanceId,
		Long sessionId,
		Long memberId,
		AttendanceStatus finalStatus
	) {
	}

}
