package me.chan99k.learningmanager.application.session;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.adapter.auth.AuthProblemCode;
import me.chan99k.learningmanager.application.UserContext;
import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.application.member.requires.MemberQueryRepository;
import me.chan99k.learningmanager.application.session.provides.SessionDeletion;
import me.chan99k.learningmanager.application.session.requires.SessionCommandRepository;
import me.chan99k.learningmanager.application.session.requires.SessionQueryRepository;
import me.chan99k.learningmanager.common.exception.AuthorizationException;
import me.chan99k.learningmanager.domain.exception.DomainException;
import me.chan99k.learningmanager.domain.member.Member;
import me.chan99k.learningmanager.domain.member.MemberProblemCode;
import me.chan99k.learningmanager.domain.member.SystemRole;
import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionProblemCode;

@Service
@Transactional
public class SessionDeletionService implements SessionDeletion {
	private final SessionQueryRepository sessionQueryRepository;
	private final SessionCommandRepository sessionCommandRepository;
	private final CourseQueryRepository courseQueryRepository;
	private final MemberQueryRepository memberQueryRepository;
	private final UserContext userContext;

	public SessionDeletionService(SessionQueryRepository sessionQueryRepository,
		SessionCommandRepository sessionCommandRepository,
		CourseQueryRepository courseQueryRepository,
		MemberQueryRepository memberQueryRepository,
		UserContext userContext) {
		this.sessionQueryRepository = sessionQueryRepository;
		this.sessionCommandRepository = sessionCommandRepository;
		this.courseQueryRepository = courseQueryRepository;
		this.memberQueryRepository = memberQueryRepository;
		this.userContext = userContext;
	}

	@Override
	public void deleteSession(Long sessionId) {
		Long currentMemberId = getCurrentMemberId();
		Session session = getSessionById(sessionId);

		validateDeletionPermission(session, currentMemberId);
		validateDeletionConstraints(session);

		sessionCommandRepository.delete(session);
	}

	private Long getCurrentMemberId() {
		return userContext.getCurrentMemberId();
	}

	private Session getSessionById(Long sessionId) {
		return sessionQueryRepository.findById(sessionId)
			.orElseThrow(() -> new DomainException(SessionProblemCode.SESSION_NOT_FOUND));
	}

	private void validateDeletionPermission(Session session, Long memberId) {
		Member member = memberQueryRepository.findById(memberId)
			.orElseThrow(() -> new DomainException(MemberProblemCode.MEMBER_NOT_FOUND));

		// 단독 세션은 시스템 관리자만 삭제 가능
		if (isStandaloneSession(session)) {
			if (!member.getRole().equals(SystemRole.ADMIN)) {
				throw new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED);
			}
			return;
		}

		// 과정/커리큘럼 세션은 해당 과정의 관리자만 삭제 가능
		if (session.getCourseId() != null) {
			courseQueryRepository.findManagedCourseById(session.getCourseId(), memberId)
				.orElseThrow(() -> new AuthorizationException(AuthProblemCode.AUTHORIZATION_REQUIRED));
		}
	}

	private void validateDeletionConstraints(Session session) {
		// 하위 세션이 있는 경우 삭제 불가
		if (!session.getChildren().isEmpty()) {
			throw new IllegalArgumentException(SessionProblemCode.CANNOT_DELETE_WHEN_CHILD_EXISTS.getMessage());
		}
	}

	private boolean isStandaloneSession(Session session) {
		return session.getCourseId() == null && session.getCurriculumId() == null;
	}
}