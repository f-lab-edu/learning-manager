package me.chan99k.learningmanager.application.attendance;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.application.attendance.provides.AttendanceCheckOut;
import me.chan99k.learningmanager.application.auth.UserContext;
import me.chan99k.learningmanager.application.session.SessionQueryRepository;
import me.chan99k.learningmanager.domain.attendance.Attendance;
import me.chan99k.learningmanager.domain.attendance.AttendanceEvent;
import me.chan99k.learningmanager.domain.attendance.CheckedOut;
import me.chan99k.learningmanager.domain.exception.AuthProblemCode;
import me.chan99k.learningmanager.domain.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.exception.DomainException;
import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionProblemCode;

@Service
@Transactional
public class AttendanceCheckOutService implements AttendanceCheckOut {
	private final AttendanceQueryRepository attendanceQueryRepository;
	private final AttendanceCommandRepository attendanceCommandRepository;
	private final SessionQueryRepository sessionQueryRepository;
	private final Clock clock;
	private final UserContext userContext;

	public AttendanceCheckOutService(AttendanceQueryRepository attendanceQueryRepository,
		AttendanceCommandRepository attendanceCommandRepository, SessionQueryRepository sessionQueryRepository,
		Clock clock, UserContext userContext) {
		this.attendanceQueryRepository = attendanceQueryRepository;
		this.attendanceCommandRepository = attendanceCommandRepository;
		this.sessionQueryRepository = sessionQueryRepository;
		this.clock = clock;
		this.userContext = userContext;
	}

	@Override
	public Response checkOut(Request request) {
		// 1. 멤버 아이디 확보
		Long currentMemberId = userContext.getCurrentMemberId();

		// 2. 대상 세션 확보
		Session session = sessionQueryRepository.findById(request.sessionId())
			.orElseThrow(() -> new DomainException(SessionProblemCode.SESSION_NOT_FOUND));

		// 3. 인가 - 세션 참여자 여부 확인
		boolean isParticipant = session.getParticipants().stream()
			.anyMatch(p -> p.getMemberId().equals(currentMemberId));

		if (!isParticipant) {
			throw new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED);
		}

		Attendance attendance = attendanceQueryRepository
			.findBySessionIdAndMemberId(request.sessionId(), currentMemberId)
			.orElseThrow(() -> new IllegalArgumentException("[System] 출석 정보가 없습니다."));

		attendance.checkOut(clock);

		Attendance saved = attendanceCommandRepository.save(attendance);

		return new AttendanceCheckOut.Response(
			saved.getId(),
			saved.getSessionId(),
			saved.getMemberId(),
			getCheckOutTime(saved), // 체크 아웃 시간 추출
			saved.getFinalStatus().name()
		);
	}

	private Instant getCheckOutTime(Attendance attendance) {
		return attendance.getEvents().stream()
			.filter(event -> event instanceof CheckedOut)
			.map(AttendanceEvent::timestamp)
			.reduce((first, second) -> second) // 최후의 체크 아웃 이벤트 반환
			.orElse(clock.instant());
	}
}
