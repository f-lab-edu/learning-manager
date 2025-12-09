package me.chan99k.learningmanager.attendance;

import java.time.Instant;

import org.springframework.util.ObjectUtils;

import me.chan99k.learningmanager.exception.DomainException;

public interface AttendanceCorrectionRequest {
	Response request(Long requestedBy, Request request);

	record Request(
		String attendanceId,
		AttendanceStatus requestedStatus,
		String reason              // 사유 (필수)
	) {
		public Request {
			if (ObjectUtils.isEmpty(reason)) {
				throw new DomainException(AttendanceProblemCode.CORRECTION_REASON_REQUIRED);
			}
		}
	}

	record Response(
		String attendanceId,
		Long sessionId,
		Long targetMemberId,
		AttendanceStatus currentStatus,
		AttendanceStatus requestedStatus,
		String reason,
		Instant requestedAt
	) {
	}
}
