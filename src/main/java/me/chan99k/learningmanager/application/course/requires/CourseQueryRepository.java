package me.chan99k.learningmanager.application.course.requires;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import me.chan99k.learningmanager.application.course.CourseDetailInfo;
import me.chan99k.learningmanager.application.course.CourseMemberInfo;
import me.chan99k.learningmanager.domain.course.Course;

public interface CourseQueryRepository {
	Optional<Course> findById(Long courseId);

	Optional<Course> findByTitle(String title);

	Optional<Course> findManagedCourseById(Long courseId, Long memberId);

	List<Course> findManagedCoursesByMemberId(Long memberId);

	Optional<CourseDetailInfo> findCourseDetailById(Long courseId);

	Page<CourseMemberInfo> findCourseMembersByCourseId(Long courseId, Pageable pageable);

}
