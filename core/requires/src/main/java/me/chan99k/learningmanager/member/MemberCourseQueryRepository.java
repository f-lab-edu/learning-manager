package me.chan99k.learningmanager.member;

import java.util.List;

import me.chan99k.learningmanager.course.Course;

public interface MemberCourseQueryRepository {

	List<Course> findManagedCoursesByMemberId(Long memberId);

	List<Course> findParticipatingCoursesByMemberId(Long memberId);

	List<CourseParticipationInfo> findParticipatingCoursesWithRoleByMemberId(Long memberId);

}