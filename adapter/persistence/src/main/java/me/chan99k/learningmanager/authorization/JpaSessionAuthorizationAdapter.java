package me.chan99k.learningmanager.authorization;

import java.util.List;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.session.JpaSessionRepository;
import me.chan99k.learningmanager.session.entity.SessionEntity;

@Repository
public class JpaSessionAuthorizationAdapter implements SessionAuthorizationPort {

	private final JpaSessionRepository sessionRepository;
	private final CourseAuthorizationPort courseAuthorizationPort;

	public JpaSessionAuthorizationAdapter(
		JpaSessionRepository sessionRepository,
		CourseAuthorizationPort courseAuthorizationPort
	) {
		this.sessionRepository = sessionRepository;
		this.courseAuthorizationPort = courseAuthorizationPort;
	}

	@Override
	public boolean hasRoleForSession(Long memberId, Long sessionId, CourseRole role) {
		return sessionRepository.findById(sessionId)
			.map(SessionEntity::getCourseId)
			.map(courseId -> courseAuthorizationPort.hasRole(memberId, courseId, role))
			.orElse(false);
	}

	@Override
	public boolean hasAnyRoleForSession(Long memberId, Long sessionId, List<CourseRole> roles) {
		return sessionRepository.findById(sessionId)
			.map(SessionEntity::getCourseId)
			.map(courseId -> courseAuthorizationPort.hasAnyRole(memberId, courseId, roles))
			.orElse(false);
	}

	@Override
	public boolean isMemberOfSession(Long memberId, Long sessionId) {
		return sessionRepository.findById(sessionId)
			.map(SessionEntity::getCourseId)
			.map(courseId -> courseAuthorizationPort.isMember(memberId, courseId))
			.orElse(false);
	}
}
