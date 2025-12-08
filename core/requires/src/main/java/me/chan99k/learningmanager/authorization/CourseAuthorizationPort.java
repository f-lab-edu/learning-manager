package me.chan99k.learningmanager.authorization;

import java.util.List;

import me.chan99k.learningmanager.course.CourseRole;

public interface CourseAuthorizationPort {

	boolean hasRole(Long memberId, Long courseId, CourseRole role);

	boolean hasAnyRole(Long memberId, Long courseId, List<CourseRole> roles);

	boolean isMember(Long memberId, Long courseId);

	boolean hasRoleForSession(Long memberId, Long sessionId, CourseRole role);

	boolean hasAnyRoleForSession(Long memberId, Long sessionId, List<CourseRole> roles);

	boolean isMemberOfSession(Long memberId, Long sessionId);
}
