package me.chan99k.learningmanager.attendance;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class AttendanceCorrectionApprovalService implements AttendanceCorrectionApproval {

	private final AttendanceQueryRepository attendanceQueryRepository;
	private final AttendanceCommandRepository attendanceCommandRepository;
	private final Clock clock;

	public AttendanceCorrectionApprovalService(
		AttendanceQueryRepository attendanceQueryRepository,
		AttendanceCommandRepository attendanceCommandRepository,
		Clock clock
	) {
		this.attendanceQueryRepository = attendanceQueryRepository;
		this.attendanceCommandRepository = attendanceCommandRepository;
		this.clock = clock;
	}

	@Override
	public Response approve(Long approvedBy, Request request) {
		// 출석 조회
		Attendance attendance = attendanceQueryRepository
			.findById(request.attendanceId())
			.orElseThrow(() -> new DomainException(AttendanceProblemCode.ATTENDANCE_NOT_FOUND));

		// 대기 중인 요청 정보 스냅샷
		CorrectionRequested pending = attendance.getPendingRequest();

		// 승인 (도메인 메서드 호출 - 상태 변경됨)
		attendance.approveCorrection(approvedBy, clock);

		attendanceCommandRepository.save(attendance);

		return new Response(
			attendance.getId(),
			pending.currentStatus(),
			pending.requestedStatus(),
			approvedBy,
			clock.instant()
		);
	}
}
