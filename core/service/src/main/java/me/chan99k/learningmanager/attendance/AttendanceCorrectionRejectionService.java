package me.chan99k.learningmanager.attendance;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.exception.DomainException;

@Service
@Transactional
public class AttendanceCorrectionRejectionService implements AttendanceCorrectionRejection {

	private final AttendanceQueryRepository attendanceQueryRepository;
	private final AttendanceCommandRepository attendanceCommandRepository;
	private final Clock clock;

	public AttendanceCorrectionRejectionService(
		AttendanceQueryRepository attendanceQueryRepository,
		AttendanceCommandRepository attendanceCommandRepository,
		Clock clock
	) {
		this.attendanceQueryRepository = attendanceQueryRepository;
		this.attendanceCommandRepository = attendanceCommandRepository;
		this.clock = clock;
	}

	@Override
	public Response reject(Long rejectedBy, Request request) {
		// 출석 조회
		Attendance attendance = attendanceQueryRepository
			.findById(request.attendanceId())
			.orElseThrow(() -> new DomainException(AttendanceProblemCode.ATTENDANCE_NOT_FOUND));

		// 거절
		attendance.rejectCorrection(
			request.rejectionReason(),
			rejectedBy,
			clock
		);

		attendanceCommandRepository.save(attendance);

		return new Response(
			attendance.getId(),
			rejectedBy,
			request.rejectionReason(),
			clock.instant()
		);
	}
}
