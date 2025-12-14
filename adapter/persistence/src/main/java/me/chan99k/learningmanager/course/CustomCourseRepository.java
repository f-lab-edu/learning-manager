package me.chan99k.learningmanager.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomCourseRepository {
	Page<CourseMemberInfo> findCourseMembersByCourseId(Long courseId, Pageable pageable);
}
