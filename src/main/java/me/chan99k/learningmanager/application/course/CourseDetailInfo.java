package me.chan99k.learningmanager.application.course;

import java.time.Instant;

public record CourseDetailInfo(
	Long courseId,
	String title,
	String description,
	Instant createdAt,
	int totalMembers,
	int totalCurricula,
	int totalSessions
) {
}