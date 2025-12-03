package me.chan99k.learningmanager.attendance;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.session.Session;
import me.chan99k.learningmanager.session.SessionProblemCode;
import me.chan99k.learningmanager.session.SessionQueryRepository;

@Service
@Transactional
public class AttendanceCheckInService implements AttendanceCheckIn {
	private final AttendanceQueryRepository attendanceQueryRepository;
	private final AttendanceCommandRepository attendanceCommandRepository;
	private final SessionQueryRepository sessionQueryRepository;
	private final Clock clock;

	public AttendanceCheckInService(
		AttendanceQueryRepository attendanceQueryRepository,
		AttendanceCommandRepository attendanceCommandRepository,
		SessionQueryRepository sessionQueryRepository, Clock clock
	) {
		this.attendanceQueryRepository = attendanceQueryRepository;
		this.attendanceCommandRepository = attendanceCommandRepository;
		this.sessionQueryRepository = sessionQueryRepository;
		this.clock = clock;
	}

	@Override
	public AttendanceCheckIn.Response checkIn(Long requestedBy, AttendanceCheckIn.Request request) {
		// 1. 세션 존재 여부 확인
		Session session = sessionQueryRepository.findById(request.sessionId())
			.orElseThrow(() -> new DomainException(SessionProblemCode.SESSION_NOT_FOUND));

		// 2. 인가 - 세션 참여자 여부 확인
		boolean isParticipant = session.getParticipants().stream()
			.anyMatch(p -> p.getMemberId().equals(requestedBy));

		if (!isParticipant) {
			throw new DomainException(SessionProblemCode.NOT_SESSION_PARTICIPANT);
		}

		Attendance attendance = attendanceQueryRepository
			.findBySessionIdAndMemberId(request.sessionId(), requestedBy) // 3. 기존 출석이 있는지 확인
			.orElseGet(() -> Attendance.create(request.sessionId(), requestedBy));

		attendance.checkIn(clock); // 5. 체크인

		Attendance savedAttendance = attendanceCommandRepository.save(attendance);

		return new Response(
			savedAttendance.getId(),
			savedAttendance.getSessionId(),
			savedAttendance.getMemberId(),
			getCheckInTime(savedAttendance), // 체크인 시간 추출
			savedAttendance.getFinalStatus().name()
		);

	}

	private java.time.Instant getCheckInTime(Attendance attendance) {
		return attendance.getEvents().stream()
			.filter(event -> event instanceof me.chan99k.learningmanager.attendance.CheckedIn)
			.map(AttendanceEvent::timestamp)
			.findFirst() // 최초 입실 시간 반환
			.orElse(clock.instant());
	}

}
