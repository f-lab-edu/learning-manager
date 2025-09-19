package me.chan99k.learningmanager.application.course;

import java.time.Instant;

import me.chan99k.learningmanager.domain.course.CourseRole;

public record CourseMemberInfo(
	Long memberId,
	String nickname,
	String email,
	CourseRole courseRole,
	Instant joinedAt
) {
}