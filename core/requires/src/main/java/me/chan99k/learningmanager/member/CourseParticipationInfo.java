package me.chan99k.learningmanager.member;

import me.chan99k.learningmanager.course.CourseRole;

public record CourseParticipationInfo(
	Long courseId,
	String title,
	String description,
	CourseRole role
) {
}