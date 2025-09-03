package me.chan99k.learningmanager.application.session.provides;

import java.time.Instant;

import me.chan99k.learningmanager.domain.session.SessionLocation;
import me.chan99k.learningmanager.domain.session.SessionType;

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
