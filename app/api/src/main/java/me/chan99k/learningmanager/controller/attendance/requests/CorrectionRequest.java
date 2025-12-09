package me.chan99k.learningmanager.controller.attendance.requests;

import me.chan99k.learningmanager.attendance.AttendanceStatus;

public record CorrectionRequest(
	AttendanceStatus requestedStatus,
	String reason
) {
}
