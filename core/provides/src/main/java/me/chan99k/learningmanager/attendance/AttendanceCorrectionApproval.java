package me.chan99k.learningmanager.attendance;

import java.time.Instant;

import org.springframework.util.ObjectUtils;

import me.chan99k.learningmanager.exception.DomainException;

public interface AttendanceCorrectionApproval {
	Response approve(Long approvedBy, Request request);

	record Request(
		String attendanceId    // 대상 출석 ID
	) {
		public Request {
			if (ObjectUtils.isEmpty(attendanceId)) {
				throw new DomainException(AttendanceProblemCode.ATTENDANCE_ID_REQUIRED);
			}
		}
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
