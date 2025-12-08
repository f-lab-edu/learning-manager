package me.chan99k.learningmanager.attendance;

import java.time.Instant;

public interface GenerateAttendanceToken {

	Response generate(Long requestedBy, Request request);

	record Request(Long sessionId) {
	}

	record Response(
		String token,
		String checkInUrl,
		String checkOutUrl,
		Instant checkInExpiresAt,    // 세션 종료 시간
		Instant checkOutExpiresAt,   // 기본적으로 자정
		Long courseId,
		Long curriculumId,
		Long sessionId,
		String sessionTitle
	) {
	}
}
