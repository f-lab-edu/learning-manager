package me.chan99k.learningmanager.attendance;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.session.Session;
import me.chan99k.learningmanager.session.SessionProblemCode;
import me.chan99k.learningmanager.session.SessionQueryRepository;

@Service
@Transactional
public class AttendanceCheckOutService implements AttendanceCheckOut {
	private final AttendanceQueryRepository attendanceQueryRepository;
	private final AttendanceCommandRepository attendanceCommandRepository;
	private final SessionQueryRepository sessionQueryRepository;
	private final Clock clock;

	public AttendanceCheckOutService(AttendanceQueryRepository attendanceQueryRepository,
		AttendanceCommandRepository attendanceCommandRepository, SessionQueryRepository sessionQueryRepository,
		Clock clock) {
		this.attendanceQueryRepository = attendanceQueryRepository;
		this.attendanceCommandRepository = attendanceCommandRepository;
		this.sessionQueryRepository = sessionQueryRepository;
		this.clock = clock;
	}

	@Override
	public Response checkOut(Long requestedBy, Request request) {
		// 1. 대상 세션 확보
		Session session = sessionQueryRepository.findById(request.sessionId())
			.orElseThrow(() -> new DomainException(SessionProblemCode.SESSION_NOT_FOUND));

		// 2. 인가 - 세션 참여자 여부 확인
		boolean isParticipant = session.getParticipants().stream()
			.anyMatch(p -> p.getMemberId().equals(requestedBy));

		if (!isParticipant) {
			throw new DomainException(SessionProblemCode.NOT_SESSION_PARTICIPANT);
		}

		Attendance attendance = attendanceQueryRepository
			.findBySessionIdAndMemberId(request.sessionId(), requestedBy)
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
