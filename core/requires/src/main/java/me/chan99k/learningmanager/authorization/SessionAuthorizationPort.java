package me.chan99k.learningmanager.authorization;

import java.util.List;

import me.chan99k.learningmanager.course.CourseRole;

public interface SessionAuthorizationPort {
	// sessionId로 조회 후 권한 확인
	boolean hasRoleForSession(Long memberId, Long sessionId, CourseRole role);

	boolean hasAnyRoleForSession(Long memberId, Long sessionId, List<CourseRole> roles);

	boolean isMemberOfSession(Long memberId, Long sessionId);

	// courseId 직접 전달 (중복 조회 방지)
	boolean hasRoleForCourse(Long memberId, Long courseId, CourseRole role);

	boolean hasAnyRoleForCourse(Long memberId, Long courseId, List<CourseRole> roles);

	boolean isMemberOfCourse(Long memberId, Long courseId);
}
