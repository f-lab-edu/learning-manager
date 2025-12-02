package me.chan99k.learningmanager.attendance;

import java.time.Instant;

public interface AttendanceCheckOut {
	AttendanceCheckOut.Response checkOut(AttendanceCheckOut.Request request);

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
