package me.chan99k.learningmanager.application.attendance.requires;

import java.util.Optional;

import me.chan99k.learningmanager.domain.attendance.Attendance;

public interface AttendanceQueryRepository {
	/**
	 * 특정 세션의 특정 멤버 출석 기록 조회
	 */
	Optional<Attendance> findBySessionIdAndMemberId(Long sessionId, Long memberId);

}
