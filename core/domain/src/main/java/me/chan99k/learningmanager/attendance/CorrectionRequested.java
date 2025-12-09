package me.chan99k.learningmanager.attendance;

import java.time.Instant;

public record CorrectionRequested(
	Instant timestamp,
	AttendanceStatus currentStatus,      // 현재 상태
	AttendanceStatus requestedStatus,    // 수정 요청 상태
	String reason,
	Long requestedBy
) implements AttendanceEvent {
}
