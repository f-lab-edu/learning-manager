package me.chan99k.learningmanager.session.dto;

import java.time.Instant;

public record SessionInfo(
	Long sessionId,
	String sessionTitle,
	Instant scheduledAt,
	Long courseId,
	String courseTitle,
	Long curriculumId,
	String curriculumTitle
) {
}