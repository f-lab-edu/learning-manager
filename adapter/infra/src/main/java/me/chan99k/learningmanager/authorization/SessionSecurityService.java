package me.chan99k.learningmanager.authorization;

import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.member.SystemRole;
import me.chan99k.learningmanager.session.Session;
import me.chan99k.learningmanager.session.SessionParticipantRole;
import me.chan99k.learningmanager.session.SessionQueryRepository;

@Service("sessionSecurity")
public class SessionSecurityService implements SessionSecurity {
	private final SessionAuthorizationPort sessionAuthorizationPort;
	private final SessionQueryRepository sessionQueryRepository;
	private final SystemAuthorizationPort systemAuthorizationPort;

	public SessionSecurityService(
		SessionAuthorizationPort sessionAuthorizationPort,
		SessionQueryRepository sessionQueryRepository,
		SystemAuthorizationPort systemAuthorizationPort
	) {
		this.sessionAuthorizationPort = sessionAuthorizationPort;
		this.sessionQueryRepository = sessionQueryRepository;
		this.systemAuthorizationPort = systemAuthorizationPort;
	}

	@Override
	public boolean isSessionManager(Long sessionId, Long memberId) {
		Session session = findSession(sessionId);
		if (session == null) {
			return false;
		}

		// 단독 세션 : 운영자 이상의 권한을 가지면 가능
		if (session.getCourseId() == null) {
			return systemAuthorizationPort.hasRoleOrHigher(memberId, SystemRole.OPERATOR);
		}

		// 과정 세션: MANAGER 권한 확인 (courseId 직접 전달)
		return sessionAuthorizationPort.hasRoleForCourse(
			memberId, session.getCourseId(), CourseRole.MANAGER
		);
	}

	@Override
	public boolean isSessionManagerOrMentor(Long sessionId, Long memberId) {
		Session session = findSession(sessionId);
		if (session == null) {
			return false;
		}

		if (session.getCourseId() == null) {
			return systemAuthorizationPort.hasRoleOrHigher(memberId, SystemRole.OPERATOR);
		}

		// 과정 세션: MANAGER 또는 MENTOR 권한 확인 (courseId 직접 전달)
		return sessionAuthorizationPort.hasAnyRoleForCourse(
			memberId, session.getCourseId(), List.of(CourseRole.MANAGER, CourseRole.MENTOR)
		);
	}

	@Override
	public boolean isSessionMember(Long sessionId, Long memberId) {
		Session session = findSession(sessionId);
		if (session == null) {
			return false;
		}

		if (session.getCourseId() == null) {
			return systemAuthorizationPort.hasRoleOrHigher(memberId, SystemRole.OPERATOR);
		}

		// courseId 직접 전달
		return sessionAuthorizationPort.isMemberOfCourse(memberId, session.getCourseId());
	}

	@Override
	public boolean canManageSessionParticipants(Long sessionId, Long memberId) {
		Session session = findSession(sessionId);
		if (session == null) {
			return false;
		}

		// 단독 세션: Level 1 이상 시스템 역할
		if (session.getCourseId() == null) {
			return systemAuthorizationPort.hasRoleOrHigher(memberId, SystemRole.OPERATOR);
		}

		// Course MANAGER 권한 (courseId 직접 전달)
		if (sessionAuthorizationPort.hasRoleForCourse(
			memberId, session.getCourseId(), CourseRole.MANAGER)) {
			return true;
		}

		// Session HOST 권한
		return session.getParticipants().stream()
			.anyMatch(p -> p.getMemberId().equals(memberId)
				&& p.getRole() == SessionParticipantRole.HOST);
	}

	@Nullable
	private Session findSession(Long sessionId) {
		return sessionQueryRepository.findById(sessionId).orElse(null);
	}
}
