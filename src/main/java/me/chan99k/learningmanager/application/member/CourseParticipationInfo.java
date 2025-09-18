package me.chan99k.learningmanager.application.member;

import me.chan99k.learningmanager.domain.course.CourseRole;

public record CourseParticipationInfo(
	Long courseId,
	String title,
	String description,
	CourseRole role
) {
}