package me.chan99k.learningmanager.application.member.requires;

import java.util.List;

import me.chan99k.learningmanager.domain.course.Course;

public interface CourseQueryRepository {

	List<Course> findManagedCoursesByMemberId(Long memberId);

}