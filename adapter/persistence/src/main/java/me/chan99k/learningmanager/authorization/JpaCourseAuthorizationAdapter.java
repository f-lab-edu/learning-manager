package me.chan99k.learningmanager.authorization;

import java.util.List;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.course.JpaCourseRepository;

@Repository
public class JpaCourseAuthorizationAdapter implements CourseAuthorizationPort {

	private final JpaCourseRepository courseRepository;

	public JpaCourseAuthorizationAdapter(JpaCourseRepository courseRepository) {
		this.courseRepository = courseRepository;
	}

	@Override
	public boolean hasRole(Long memberId, Long courseId, CourseRole role) {
		return courseRepository.existsByMemberIdAndCourseIdAndRole(memberId, courseId, role);
	}

	@Override
	public boolean hasAnyRole(Long memberId, Long courseId, List<CourseRole> roles) {
		return courseRepository.existsByMemberIdAndCourseIdAndRoleIn(memberId, courseId, roles);
	}

	@Override
	public boolean isMember(Long memberId, Long courseId) {
		return courseRepository.existsByMemberIdAndCourseId(memberId, courseId);
	}
}
