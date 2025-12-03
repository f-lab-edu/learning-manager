package me.chan99k.learningmanager.session;

import java.time.Instant;

public interface SessionDetailRetrieval {
	record Response(
		Long id,
		String title,
		Instant scheduledAt,
		Instant scheduledEndAt,
		SessionType type,
		SessionLocation location,
		String locationDetails,
		Long courseId,
		Long curriculumId,
		Long parentId,
		Integer childrenCount,
		Integer participantCount
	) {
	}
}
