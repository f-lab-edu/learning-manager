package me.chan99k.learningmanager.application.session;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.application.auth.requires.UserContext;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.application.session.provides.SessionUpdate;
import me.chan99k.learningmanager.application.session.requires.SessionCommandRepository;
import me.chan99k.learningmanager.application.session.requires.SessionQueryRepository;
import me.chan99k.learningmanager.domain.exception.AuthProblemCode;
import me.chan99k.learningmanager.domain.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.SystemRole;
import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionProblemCode;

@Service
@Transactional
public class SessionUpdateService implements SessionUpdate {
	private final SessionQueryRepository sessionQueryRepository;
	private final SessionCommandRepository sessionCommandRepository;
	private final CourseQueryRepository courseQueryRepository;
	private final MemberQueryRepository memberQueryRepository;
	private final Clock clock;
	private final UserContext userContext;

	public SessionUpdateService(SessionQueryRepository sessionQueryRepository,
		SessionCommandRepository sessionCommandRepository,
		CourseQueryRepository courseQueryRepository,
		MemberQueryRepository memberQueryRepository,
		Clock clock,
		UserContext userContext) {
		this.sessionQueryRepository = sessionQueryRepository;
		this.sessionCommandRepository = sessionCommandRepository;
		this.courseQueryRepository = courseQueryRepository;
		this.memberQueryRepository = memberQueryRepository;
		this.clock = clock;
		this.userContext = userContext;
	}

	@Override
	public void updateSession(Long sessionId, Request request) {
		Long currentMemberId = getCurrentMemberId();
		Session session = getSessionById(sessionId);

		validateUpdatePermission(session, currentMemberId);

		updateSessionInfo(session, request);

		sessionCommandRepository.save(session);
	}

	private Long getCurrentMemberId() {
		return userContext.getCurrentMemberId();
	}

	private Session getSessionById(Long sessionId) {
		return sessionQueryRepository.findById(sessionId)
			.orElseThrow(() -> new DomainException(SessionProblemCode.SESSION_NOT_FOUND));
	}

	private void validateUpdatePermission(Session session, Long memberId) {
		Member member = memberQueryRepository.findById(memberId)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		// 단독 세션은 시스템 관리자가 수정 가능
		if (isStandaloneSession(session)) {
			if (!member.getRole().equals(SystemRole.ADMIN)) {
				throw new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED);
			}
			return;
		}

		// 과정/커리큘럼 세션은 해당 과정의 관리자가 수정 가능
		if (session.getCourseId() != null) {
			courseQueryRepository.findManagedCourseById(session.getCourseId(), memberId)
				.orElseThrow(() -> new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));
		}
	}

	private void updateSessionInfo(Session session, Request request) {
		// 시간 변경
		session.reschedule(request.scheduledAt(), request.scheduledEndAt(), clock);

		// 기본 정보 변경
		session.changeInfo(request.title(), request.type(), clock);

		// 장소 변경
		session.changeLocation(request.location(), request.locationDetails(), clock);
	}

	private boolean isStandaloneSession(Session session) {
		return session.getCourseId() == null && session.getCurriculumId() == null;
	}
}