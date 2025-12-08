package me.chan99k.learningmanager.authorization;

import java.util.List;

import org.springframework.stereotype.Service;

import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.session.SessionQueryRepository;

@Service("courseSecurity")
public class CourseSecurityService {
	private final CourseAuthorizationPort authorizationPort;
	private final SessionQueryRepository sessionQueryRepository;

	public CourseSecurityService(CourseAuthorizationPort authorizationPort,
		SessionQueryRepository sessionQueryRepository) {
		this.authorizationPort = authorizationPort;
		this.sessionQueryRepository = sessionQueryRepository;
	}

	public boolean isManager(Long courseId, Long memberId) {
		return authorizationPort.hasRole(memberId, courseId, CourseRole.MANAGER);
	}

	public boolean isManagerOrMentor(Long courseId, Long memberId) {
		return authorizationPort.hasAnyRole(
			memberId,
			courseId,
			List.of(CourseRole.MANAGER, CourseRole.MENTOR)
		);
	}

	public boolean isMember(Long courseId, Long memberId) {
		return authorizationPort.isMember(memberId, courseId);
	}
}
