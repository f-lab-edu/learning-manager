package me.chan99k.learningmanager.application.attendance.provides;

import java.time.Instant;

public interface AttendanceCheckIn {
	Response checkIn(Request request);

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
