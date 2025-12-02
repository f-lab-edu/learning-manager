package me.chan99k.learningmanager.course;

import java.time.Instant;

public record CourseMemberInfo(
	Long memberId,
	String nickname,
	String email,
	CourseRole courseRole,
	Instant joinedAt
) {
}