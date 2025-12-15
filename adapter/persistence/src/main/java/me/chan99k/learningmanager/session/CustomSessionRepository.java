package me.chan99k.learningmanager.session;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.session.dto.SessionInfo;
import me.chan99k.learningmanager.session.entity.SessionEntity;

public interface CustomSessionRepository {

	Optional<SessionEntity> findManagedSessionById(Long sessionId, Long memberId, CourseRole courseRole);

	Page<SessionEntity> findAllWithFilters(
		SessionType type,
		SessionLocation location,
		Instant startDate,
		Instant endDate,
		Pageable pageable
	);

	Page<SessionEntity> findByCourseIdWithFilters(
		Long courseId,
		SessionType type,
		SessionLocation location,
		Instant startDate,
		Instant endDate,
		Boolean includeChildSessions,
		Pageable pageable
	);

	Page<SessionEntity> findByCurriculumIdWithFilters(
		Long curriculumId,
		SessionType type,
		SessionLocation location,
		Instant startDate,
		Instant endDate,
		Boolean includeChildSessions,
		Pageable pageable
	);

	Page<SessionEntity> findByMemberIdWithFilters(
		Long memberId,
		SessionType type,
		SessionLocation location,
		Instant startDate,
		Instant endDate,
		Pageable pageable
	);

	List<SessionEntity> findByYearMonth(
		Instant startOfMonth,
		Instant startOfNextMonth,
		SessionType type,
		SessionLocation location,
		Long courseId,
		Long curriculumId
	);

	List<Long> findIdsByPeriodAndFilters(
		Instant startDate,
		Instant endDate,
		Long courseId,
		Long curriculumId
	);

	List<SessionInfo> findSessionInfoProjectionByIds(List<Long> sessionIds);
}
