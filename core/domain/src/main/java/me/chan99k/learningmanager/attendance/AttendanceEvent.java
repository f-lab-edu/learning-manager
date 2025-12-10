package me.chan99k.learningmanager.attendance;

import java.time.Clock;
import java.time.Instant;

public sealed interface AttendanceEvent
	permits CheckedIn, CheckedOut, CorrectionRejected, CorrectionRequested, StatusCorrected {

	static CheckedIn checkIn(Clock clock) {
		return new CheckedIn(clock.instant());
	}

	static CheckedOut checkOut(Clock clock) {
		return new CheckedOut(clock.instant());
	}

	static StatusCorrected statusCorrected(
		Clock clock,
		AttendanceStatus previousStatus,
		AttendanceStatus newStatus,
		String reason,
		Long correctedBy
	) {
		return new StatusCorrected(
			clock.instant(),
			previousStatus,
			newStatus,
			reason,
			correctedBy
		);
	}

	static CorrectionRequested correctionRequested(
		Clock clock,
		AttendanceStatus currentStatus,
		AttendanceStatus requestedStatus,
		String reason,
		Long requestedBy
	) {
		return new CorrectionRequested(
			clock.instant(),
			currentStatus,
			requestedStatus,
			reason,
			requestedBy
		);
	}

	static CorrectionRejected correctionRejected(
		Clock clock,
		String rejectionReason,
		Long rejectedBy
	) {
		return new CorrectionRejected(
			clock.instant(),
			rejectionReason,
			rejectedBy
		);
	}

	Instant timestamp();
}
