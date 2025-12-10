package me.chan99k.learningmanager.attendance;

import java.time.Instant;

public record StatusCorrected(
	Instant timestamp,
	AttendanceStatus previousStatus,
	AttendanceStatus newStatus,
	String reason,
	Long correctedBy
) implements AttendanceEvent {
}
