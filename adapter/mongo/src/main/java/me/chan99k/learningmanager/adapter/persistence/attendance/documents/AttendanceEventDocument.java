package me.chan99k.learningmanager.adapter.persistence.attendance.documents;

import java.time.Instant;

import me.chan99k.learningmanager.attendance.AttendanceEvent;
import me.chan99k.learningmanager.attendance.AttendanceStatus;
import me.chan99k.learningmanager.attendance.CheckedIn;
import me.chan99k.learningmanager.attendance.CheckedOut;
import me.chan99k.learningmanager.attendance.CorrectionRejected;
import me.chan99k.learningmanager.attendance.CorrectionRequested;
import me.chan99k.learningmanager.attendance.StatusCorrected;

public record AttendanceEventDocument(
	String type,
	Instant timestamp,
	AttendanceStatus previousStatus,
	AttendanceStatus newStatus,
	String reason,
	Long actorId,
	String rejectionReason
) {
	public static AttendanceEventDocument from(AttendanceEvent event) {
		return switch (event) {
			case CheckedIn e -> new AttendanceEventDocument(
				"CheckedIn", e.timestamp(),
				null, null, null, null, null
			);

			case CheckedOut e -> new AttendanceEventDocument(
				"CheckedOut", e.timestamp(),
				null, null, null, null, null
			);

			case CorrectionRequested e -> new AttendanceEventDocument(
				"CorrectionRequested", e.timestamp(),
				e.currentStatus(), e.requestedStatus(), e.reason(), e.requestedBy(), null
			);

			case StatusCorrected e -> new AttendanceEventDocument(
				"StatusCorrected", e.timestamp(),
				e.previousStatus(), e.newStatus(), e.reason(), e.correctedBy(), null
			);

			case CorrectionRejected e -> new AttendanceEventDocument(
				"CorrectionRejected", e.timestamp(),
				null, null, null, e.rejectedBy(), e.rejectionReason()
			);
		};
	}

	public AttendanceEvent toDomain() {
		return switch (this.type) {
			case "CheckedIn" -> new CheckedIn(this.timestamp);

			case "CheckedOut" -> new CheckedOut(this.timestamp);

			case "CorrectionRequested" -> new CorrectionRequested(
				this.timestamp, this.previousStatus, this.newStatus,
				this.reason, this.actorId
			);

			case "StatusCorrected" -> new StatusCorrected(
				this.timestamp, this.previousStatus, this.newStatus,
				this.reason, this.actorId
			);

			case "CorrectionRejected" -> new CorrectionRejected(
				this.timestamp, this.rejectionReason, this.actorId
			);

			default -> throw new IllegalArgumentException(
				"[System] 유효하지 않은 출석 이벤트 타입입니다: " + this.type
			);
		};
	}
}
