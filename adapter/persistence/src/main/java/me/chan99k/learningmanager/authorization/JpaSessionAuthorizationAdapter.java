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

	// === sessionId 기반 메서드 (courseId 조회 후 위임) ===

	@Override
	public boolean hasRoleForSession(Long memberId, Long sessionId, CourseRole role) {
		return sessionRepository.findById(sessionId)
			.map(SessionEntity::getCourseId)
			.map(courseId -> hasRoleForCourse(memberId, courseId, role))
			.orElse(false);
	}

	@Override
	public boolean hasAnyRoleForSession(Long memberId, Long sessionId, List<CourseRole> roles) {
		return sessionRepository.findById(sessionId)
			.map(SessionEntity::getCourseId)
			.map(courseId -> hasAnyRoleForCourse(memberId, courseId, roles))
			.orElse(false);
	}

	@Override
	public boolean isMemberOfSession(Long memberId, Long sessionId) {
		return sessionRepository.findById(sessionId)
			.map(SessionEntity::getCourseId)
			.map(courseId -> isMemberOfCourse(memberId, courseId))
			.orElse(false);
	}

	// === courseId 직접 전달 (중복 조회 방지) ===

	@Override
	public boolean hasRoleForCourse(Long memberId, Long courseId, CourseRole role) {
		return courseAuthorizationPort.hasRole(memberId, courseId, role);
	}

	@Override
	public boolean hasAnyRoleForCourse(Long memberId, Long courseId, List<CourseRole> roles) {
		return courseAuthorizationPort.hasAnyRole(memberId, courseId, roles);
	}

	@Override
	public boolean isMemberOfCourse(Long memberId, Long courseId) {
		return courseAuthorizationPort.isMember(memberId, courseId);
	}
}
