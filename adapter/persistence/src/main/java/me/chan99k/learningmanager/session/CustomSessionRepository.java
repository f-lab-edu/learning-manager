package me.chan99k.learningmanager.session;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import me.chan99k.learningmanager.session.dto.SessionInfo;
import me.chan99k.learningmanager.session.entity.SessionEntity;

public interface CustomSessionRepository {

	Optional<SessionEntity> findManagedSessionById(@Param("sessionId") Long sessionId,
		@Param("memberId") Long memberId,
		@Param("courseRole") me.chan99k.learningmanager.course.CourseRole courseRole);

	Page<SessionEntity> findAllWithFilters(
		@Param("type") SessionType type,
		@Param("location") SessionLocation location,
		@Param("startDate") Instant startDate,
		@Param("endDate") Instant endDate,
		Pageable pageable
	);

	Page<SessionEntity> findByCourseIdWithFilters(
		@Param("courseId") Long courseId,
		@Param("type") SessionType type,
		@Param("location") SessionLocation location,
		@Param("startDate") Instant startDate,
		@Param("endDate") Instant endDate,
		@Param("includeChildSessions") Boolean includeChildSessions,
		Pageable pageable
	);

	Page<SessionEntity> findByCurriculumIdWithFilters(
		@Param("curriculumId") Long curriculumId,
		@Param("type") SessionType type,
		@Param("location") SessionLocation location,
		@Param("startDate") Instant startDate,
		@Param("endDate") Instant endDate,
		@Param("includeChildSessions") Boolean includeChildSessions,
		Pageable pageable
	);

	Page<SessionEntity> findByMemberIdWithFilters(
		@Param("memberId") Long memberId,
		@Param("type") SessionType type,
		@Param("location") SessionLocation location,
		@Param("startDate") Instant startDate,
		@Param("endDate") Instant endDate,
		Pageable pageable
	);

	List<SessionEntity> findByYearMonth(
		@Param("startOfMonth") Instant startOfMonth,
		@Param("startOfNextMonth") Instant startOfNextMonth,
		@Param("type") SessionType type,
		@Param("location") SessionLocation location,
		@Param("courseId") Long courseId,
		@Param("curriculumId") Long curriculumId
	);

	List<Long> findIdsByPeriodAndFilters(
		@Param("startDate") Instant startDate,
		@Param("endDate") Instant endDate,
		@Param("courseId") Long courseId,
		@Param("curriculumId") Long curriculumId
	);

	List<SessionInfo> findSessionInfoProjectionByIds(@Param("sessionIds") List<Long> sessionIds);
}
