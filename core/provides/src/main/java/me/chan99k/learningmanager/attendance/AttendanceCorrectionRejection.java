package me.chan99k.learningmanager.attendance;

import java.time.Instant;

import org.springframework.util.ObjectUtils;

import me.chan99k.learningmanager.exception.DomainException;

public interface AttendanceCorrectionRejection {

	Response reject(Long rejectedBy, Request request);

	record Request(
		String attendanceId,       // 대상 출석 ID
		String rejectionReason     // 거절 사유 (필수)
	) {
		public Request {
			if (ObjectUtils.isEmpty(rejectionReason)) {
				throw new DomainException(AttendanceProblemCode.REJECTION_REASON_REQUIRED);
			}
		}
	}

	record Response(
		String attendanceId,
		Long rejectedBy,
		String rejectionReason,
		Instant rejectedAt
	) {
	}
}
