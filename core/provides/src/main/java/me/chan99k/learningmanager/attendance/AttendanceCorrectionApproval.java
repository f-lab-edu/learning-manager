package me.chan99k.learningmanager.attendance;

import java.time.Instant;

public interface AttendanceCorrectionApproval {
	Response approve(Long approvedBy, Request request);

	record Request(
		String attendanceId    // 대상 출석 ID
	) {
	}

	record Response(
		String attendanceId,
		AttendanceStatus previousStatus,
		AttendanceStatus newStatus,
		Long approvedBy,
		Instant approvedAt
	) {
	}
}
