package me.chan99k.learningmanager.attendance;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class AttendanceCorrectionRequestService implements AttendanceCorrectionRequest {

	private final AttendanceQueryRepository attendanceQueryRepository;
	private final AttendanceCommandRepository attendanceCommandRepository;
	private final Clock clock;

	public AttendanceCorrectionRequestService(AttendanceQueryRepository attendanceQueryRepository,
		AttendanceCommandRepository attendanceCommandRepository, Clock clock) {
		this.attendanceQueryRepository = attendanceQueryRepository;
		this.attendanceCommandRepository = attendanceCommandRepository;
		this.clock = clock;
	}

	@Override
	public Response request(Long requestedBy, Request request) {
		// 출석 조회
		Attendance attendance = attendanceQueryRepository
			.findById(request.attendanceId())
			.orElseThrow(() -> new DomainException(AttendanceProblemCode.ATTENDANCE_NOT_FOUND));

		// 현재 상태 스냅샷
		AttendanceStatus currentStatus = attendance.getFinalStatus();

		// 수정 요청
		attendance.requestCorrection(
			request.requestedStatus(),
			request.reason(),
			requestedBy,
			clock
		);

		Attendance saved = attendanceCommandRepository.save(attendance);

		return new Response(
			saved.getId(),
			saved.getSessionId(),
			saved.getMemberId(),
			currentStatus,
			request.requestedStatus(),
			request.reason(),
			clock.instant()
		);
	}
}
