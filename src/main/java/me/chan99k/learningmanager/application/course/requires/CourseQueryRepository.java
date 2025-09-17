package me.chan99k.learningmanager.application.course.requires;

import java.util.List;
import java.util.Optional;

import me.chan99k.learningmanager.domain.course.Course;

public interface CourseQueryRepository {
	Optional<Course> findById(Long courseId);

	Optional<Course> findByTitle(String title);

	Optional<Course> findManagedCourseById(Long courseId, Long memberId);

	List<Course> findManagedCoursesByMemberId(Long memberId);

}
