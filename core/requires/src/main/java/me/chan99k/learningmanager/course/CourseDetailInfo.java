package me.chan99k.learningmanager.course;

import java.time.Instant;

public record CourseDetailInfo(
	Long courseId,
	String title,
	String description,
	Instant createdAt,
	Long totalMembers,
	Long totalCurricula
) {
}