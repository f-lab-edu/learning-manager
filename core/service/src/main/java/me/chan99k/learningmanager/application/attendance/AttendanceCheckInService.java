package me.chan99k.learningmanager.application.attendance;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.application.attendance.provides.AttendanceCheckIn;
import me.chan99k.learningmanager.application.auth.UserContext;
import me.chan99k.learningmanager.application.session.SessionQueryRepository;
import me.chan99k.learningmanager.domain.attendance.Attendance;
import me.chan99k.learningmanager.domain.attendance.AttendanceEvent;
import me.chan99k.learningmanager.domain.exception.AuthProblemCode;
import me.chan99k.learningmanager.domain.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.exception.DomainException;
import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionProblemCode;

@Service
@Transactional
public class AttendanceCheckInService implements AttendanceCheckIn {
	private final AttendanceQueryRepository attendanceQueryRepository;
	private final AttendanceCommandRepository attendanceCommandRepository;
	private final SessionQueryRepository sessionQueryRepository;
	private final Clock clock;
	private final UserContext userContext;

	public AttendanceCheckInService(
		AttendanceQueryRepository attendanceQueryRepository,
		AttendanceCommandRepository attendanceCommandRepository,
		SessionQueryRepository sessionQueryRepository, Clock clock,
		UserContext userContext
	) {
		this.attendanceQueryRepository = attendanceQueryRepository;
		this.attendanceCommandRepository = attendanceCommandRepository;
		this.sessionQueryRepository = sessionQueryRepository;
		this.clock = clock;
		this.userContext = userContext;
	}

	@Override
	public AttendanceCheckIn.Response checkIn(AttendanceCheckIn.Request request) {
		// 1. 멤버 아이디 확보
		Long currentMemberId = userContext.getCurrentMemberId();

		// 2. 세션 존재 여부 확인
		Session session = sessionQueryRepository.findById(request.sessionId())
			.orElseThrow(() -> new DomainException(SessionProblemCode.SESSION_NOT_FOUND));

		// 3. 인가 - 세션 참여자 여부 확인
		boolean isParticipant = session.getParticipants().stream()
			.anyMatch(p -> p.getMemberId().equals(currentMemberId));

		if (!isParticipant) {
			throw new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED);
		}

		Attendance attendance = attendanceQueryRepository
			.findBySessionIdAndMemberId(request.sessionId(), currentMemberId) // 4. 기존 출석이 있는지 확인
			.orElseGet(() -> Attendance.create(request.sessionId(), currentMemberId));

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
			.filter(event -> event instanceof me.chan99k.learningmanager.domain.attendance.CheckedIn)
			.map(AttendanceEvent::timestamp)
			.findFirst() // 최초 입실 시간 반환
			.orElse(clock.instant());
	}

}
