package me.chan99k.learningmanager.session;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.auth.UserContext;
import me.chan99k.learningmanager.course.CourseProblemCode;
import me.chan99k.learningmanager.course.CourseQueryRepository;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.Member;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.member.MemberQueryRepository;
import me.chan99k.learningmanager.member.SystemRole;

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
				throw new DomainException(MemberProblemCode.ADMIN_ONLY_ACTION);
			}
			return;
		}

		// 과정/커리큘럼 세션은 해당 과정의 관리자가 수정 가능
		if (session.getCourseId() != null) {
			courseQueryRepository.findManagedCourseById(session.getCourseId(), memberId)
				.orElseThrow(() -> new DomainException(CourseProblemCode.NOT_COURSE_MANAGER));
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