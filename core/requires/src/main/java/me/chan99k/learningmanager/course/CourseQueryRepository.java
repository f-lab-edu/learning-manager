package me.chan99k.learningmanager.course;

import java.util.List;
import java.util.Optional;

import me.chan99k.learningmanager.common.PageRequest;
import me.chan99k.learningmanager.common.PageResult;

public interface CourseQueryRepository {
	Optional<Course> findById(Long courseId);

	Optional<Course> findByTitle(String title);

	Optional<Course> findManagedCourseById(Long courseId, Long memberId);

	List<Course> findManagedCoursesByMemberId(Long memberId);

	Optional<CourseDetailInfo> findCourseDetailById(Long courseId);

	PageResult<CourseMemberInfo> findCourseMembersByCourseId(Long courseId, PageRequest pageRequest);

}
