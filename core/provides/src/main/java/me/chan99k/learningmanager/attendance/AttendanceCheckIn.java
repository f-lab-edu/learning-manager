package me.chan99k.learningmanager.attendance;

import java.time.Instant;

public interface AttendanceCheckIn {
	Response checkIn(Long requestedBy, Request request);

	record Request(
		Long sessionId
	) {
	}

	record Response(
		String attendanceId,
		Long sessionId,
		Long memberId,
		Instant checkInTime,
		String status
	) {
	}
}
