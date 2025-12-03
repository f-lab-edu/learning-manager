package me.chan99k.learningmanager.member;

import java.util.List;

public interface MemberCourseParticipation {

	Response getParticipatingCourses(Long memberId);

	record Response(
		List<CourseParticipationInfo> courses
	) {
	}
}
