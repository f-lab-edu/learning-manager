package me.chan99k.learningmanager.application.session.requires;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionLocation;
import me.chan99k.learningmanager.domain.session.SessionType;

public interface SessionQueryRepository {
	Optional<Session> findById(Long sessionId);

	List<Session> findByCourseId(Long courseId);

	List<Session> findByCurriculumId(Long curriculumId);

	List<Session> findByParentId(Long parentId);

	Optional<Session> findManagedSessionById(Long sessionId, Long memberId);

	Page<Session> findAllWithFilters(SessionType type, SessionLocation location,
		Instant startDate, Instant endDate, Pageable pageable);

	Page<Session> findByCourseIdWithFilters(Long courseId, SessionType type,
		SessionLocation location, Instant startDate, Instant endDate,
		Boolean includeChildSessions, Pageable pageable);

	Page<Session> findByCurriculumIdWithFilters(Long curriculumId, SessionType type,
		SessionLocation location, Instant startDate, Instant endDate,
		Boolean includeChildSessions, Pageable pageable);
}
