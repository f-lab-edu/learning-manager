package me.chan99k.learningmanager.authorization;

import java.util.List;

import me.chan99k.learningmanager.course.CourseRole;

public interface SessionAuthorizationPort {
	boolean hasRoleForSession(Long memberId, Long sessionId, CourseRole role);

	boolean hasAnyRoleForSession(Long memberId, Long sessionId, List<CourseRole> roles);

	boolean isMemberOfSession(Long memberId, Long sessionId);
}
