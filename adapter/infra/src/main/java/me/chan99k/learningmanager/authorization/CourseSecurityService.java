package me.chan99k.learningmanager.authorization;

import java.util.List;

import org.springframework.stereotype.Service;

import me.chan99k.learningmanager.course.CourseRole;

@Service("courseSecurity")
public class CourseSecurityService implements CourseSecurity {
	private final CourseAuthorizationPort authorizationPort;

	public CourseSecurityService(CourseAuthorizationPort authorizationPort) {
		this.authorizationPort = authorizationPort;
	}

	@Override
	public boolean isManager(Long courseId, Long memberId) {
		return authorizationPort.hasRole(memberId, courseId, CourseRole.MANAGER);
	}

	@Override
	public boolean isManagerOrMentor(Long courseId, Long memberId) {
		return authorizationPort.hasAnyRole(
			memberId,
			courseId,
			List.of(CourseRole.MANAGER, CourseRole.MENTOR)
		);
	}

	@Override
	public boolean isMember(Long courseId, Long memberId) {
		return authorizationPort.isMember(memberId, courseId);
	}
}
