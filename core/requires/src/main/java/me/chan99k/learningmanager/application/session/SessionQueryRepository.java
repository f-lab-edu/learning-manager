package me.chan99k.learningmanager.application.session;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import me.chan99k.learningmanager.application.session.dto.SessionInfo;
import me.chan99k.learningmanager.common.PageRequest;
import me.chan99k.learningmanager.common.PageResult;
import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionLocation;
import me.chan99k.learningmanager.domain.session.SessionType;

public interface SessionQueryRepository {
	Optional<Session> findById(Long sessionId);

	List<Session> findByCourseId(Long courseId);

	List<Session> findByCurriculumId(Long curriculumId);

	List<Session> findByParentId(Long parentId);

	Optional<Session> findManagedSessionById(Long sessionId, Long memberId);

	PageResult<Session> findAllWithFilters(SessionType type, SessionLocation location,
		Instant startDate, Instant endDate, PageRequest pageRequest);

	PageResult<Session> findByCourseIdWithFilters(Long courseId, SessionType type,
		SessionLocation location, Instant startDate, Instant endDate,
		Boolean includeChildSessions, PageRequest pageRequest);

	PageResult<Session> findByCurriculumIdWithFilters(Long curriculumId, SessionType type,
		SessionLocation location, Instant startDate, Instant endDate,
		Boolean includeChildSessions, PageRequest pageRequest);

	PageResult<Session> findByMemberIdWithFilters(Long memberId, SessionType type,
		SessionLocation location, Instant startDate, Instant endDate, PageRequest pageRequest);

	List<Session> findByYearMonth(YearMonth yearMonth, SessionType type, SessionLocation location,
		Long courseId, Long curriculumId);

	List<Long> findSessionIdsByPeriodAndFilters(
		Instant startDate,
		Instant endDate,
		Long courseId,      // nullable
		Long curriculumId   // nullable
	);

	List<Long> findSessionIdsByCourseId(Long courseId);

	List<Long> findSessionIdsByCurriculumId(Long curriculumId);

	List<Long> findSessionIdsByMemberId(Long memberId);

	List<Long> findSessionIdsByMonthAndFilters(
		int year,
		int month,
		Long courseId,      // nullable
		Long curriculumId   // nullable
	);

	List<SessionInfo> findSessionInfoProjectionByIds(List<Long> sessionIds);

	List<SessionInfo> findSessionInfoByIds(List<Long> sessionIds);

	Map<Long, SessionInfo> findSessionInfoMapByIds(List<Long> sessionIds);

}
