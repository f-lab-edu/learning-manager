package me.chan99k.learningmanager.attendance;

import java.time.Instant;

public record CorrectionRejected(Instant timestamp, String rejectionReason, Long rejectedBy)
	implements AttendanceEvent {
}
