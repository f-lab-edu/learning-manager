package me.chan99k.learningmanager.session;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.authorization.SystemAuthorizationPort;
import me.chan99k.learningmanager.course.CourseProblemCode;
import me.chan99k.learningmanager.course.CourseQueryRepository;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.member.MemberProblemCode;
import me.chan99k.learningmanager.member.SystemRole;

@Service
@Transactional
public class SessionDeletionService implements SessionDeletion {

	private final SessionQueryRepository sessionQueryRepository;
	private final SessionCommandRepository sessionCommandRepository;
	private final CourseQueryRepository courseQueryRepository;
	private final SystemAuthorizationPort systemAuthorizationPort;

	public SessionDeletionService(
		SessionQueryRepository sessionQueryRepository,
		SessionCommandRepository sessionCommandRepository,
		CourseQueryRepository courseQueryRepository,
		SystemAuthorizationPort systemAuthorizationPort
	) {
		this.sessionQueryRepository = sessionQueryRepository;
		this.sessionCommandRepository = sessionCommandRepository;
		this.courseQueryRepository = courseQueryRepository;
		this.systemAuthorizationPort = systemAuthorizationPort;
	}

	@Override
	public void deleteSession(Long requestedBy, Long sessionId) {
		Session session = getSessionById(sessionId);

		validateDeletionPermission(session, requestedBy);
		validateDeletionConstraints(session);

		sessionCommandRepository.delete(session);
	}

	private Session getSessionById(Long sessionId) {
		return sessionQueryRepository.findById(sessionId)
			.orElseThrow(() -> new DomainException(SessionProblemCode.SESSION_NOT_FOUND));
	}

	private void validateDeletionPermission(Session session, Long memberId) {
		// 단독 세션은 시스템 관리자만 삭제 가능
		if (isStandaloneSession(session)) {
			if (!systemAuthorizationPort.hasRole(memberId, SystemRole.ADMIN)) {
				throw new DomainException(MemberProblemCode.ADMIN_ONLY_ACTION);
			}
			return;
		}

		// 과정/커리큘럼 세션은 해당 과정의 관리자만 삭제 가능
		if (session.getCourseId() != null) {
			courseQueryRepository.findManagedCourseById(session.getCourseId(), memberId)
				.orElseThrow(() -> new DomainException(CourseProblemCode.NOT_COURSE_MANAGER));
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