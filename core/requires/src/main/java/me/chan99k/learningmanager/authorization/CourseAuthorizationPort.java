package me.chan99k.learningmanager.authorization;

import java.util.List;

import me.chan99k.learningmanager.course.CourseRole;

public interface CourseAuthorizationPort {

	boolean hasRole(Long memberId, Long courseId, CourseRole role);

	boolean hasAnyRole(Long memberId, Long courseId, List<CourseRole> roles);

	boolean isMember(Long memberId, Long courseId);

}
