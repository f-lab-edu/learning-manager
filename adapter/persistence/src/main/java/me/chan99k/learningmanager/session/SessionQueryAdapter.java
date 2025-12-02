package me.chan99k.learningmanager.session;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.common.PageRequest;
import me.chan99k.learningmanager.common.PageResult;
import me.chan99k.learningmanager.common.SortOrder;
import me.chan99k.learningmanager.course.CourseRole;
import me.chan99k.learningmanager.session.dto.SessionInfo;
import me.chan99k.learningmanager.session.entity.SessionEntity;
import me.chan99k.learningmanager.session.mapper.SessionMapper;

@Repository
public class SessionQueryAdapter implements SessionQueryRepository {
	private final SessionJpaRepository jpaRepository;

	public SessionQueryAdapter(SessionJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Optional<Session> findById(Long sessionId) {
		return jpaRepository.findById(sessionId)
			.map(SessionMapper::toDomain);
	}

	@Override
	public List<Session> findByCourseId(Long courseId) {
		return jpaRepository.findByCourseId(courseId).stream()
			.map(SessionMapper::toDomain)
			.toList();
	}

	@Override
	public List<Session> findByCurriculumId(Long curriculumId) {
		return jpaRepository.findByCurriculumId(curriculumId).stream()
			.map(SessionMapper::toDomain)
			.toList();
	}

	@Override
	public List<Session> findByParentId(Long parentId) {
		return jpaRepository.findByParentId(parentId).stream()
			.map(SessionMapper::toDomain)
			.toList();
	}

	@Override
	public Optional<Session> findManagedSessionById(Long sessionId, Long memberId) {
		return jpaRepository.findManagedSessionById(sessionId, memberId, CourseRole.MANAGER)
			.map(SessionMapper::toDomain);
	}

	@Override
	public PageResult<Session> findAllWithFilters(SessionType type, SessionLocation location,
		Instant startDate, Instant endDate, PageRequest pageRequest) {
		Pageable pageable = toSpringPageable(pageRequest);
		Page<SessionEntity> page = jpaRepository.findAllWithFilters(type, location, startDate, endDate, pageable);
		List<Session> content = page.getContent().stream()
			.map(SessionMapper::toDomain)
			.toList();
		return PageResult.of(content, pageRequest, page.getTotalElements());
	}

	@Override
	public PageResult<Session> findByCourseIdWithFilters(Long courseId, SessionType type,
		SessionLocation location, Instant startDate, Instant endDate,
		Boolean includeChildSessions, PageRequest pageRequest) {
		Pageable pageable = toSpringPageable(pageRequest);
		Page<SessionEntity> page = jpaRepository.findByCourseIdWithFilters(courseId, type, location,
			startDate, endDate, includeChildSessions, pageable);
		List<Session> content = page.getContent().stream()
			.map(SessionMapper::toDomain)
			.toList();
		return PageResult.of(content, pageRequest, page.getTotalElements());
	}

	@Override
	public PageResult<Session> findByCurriculumIdWithFilters(Long curriculumId, SessionType type,
		SessionLocation location, Instant startDate, Instant endDate,
		Boolean includeChildSessions, PageRequest pageRequest) {
		Pageable pageable = toSpringPageable(pageRequest);
		Page<SessionEntity> page = jpaRepository.findByCurriculumIdWithFilters(curriculumId, type, location,
			startDate, endDate, includeChildSessions, pageable);
		List<Session> content = page.getContent().stream()
			.map(SessionMapper::toDomain)
			.toList();
		return PageResult.of(content, pageRequest, page.getTotalElements());
	}

	@Override
	public PageResult<Session> findByMemberIdWithFilters(Long memberId, SessionType type,
		SessionLocation location, Instant startDate, Instant endDate, PageRequest pageRequest) {
		Pageable pageable = toSpringPageable(pageRequest);
		Page<SessionEntity> page = jpaRepository.findByMemberIdWithFilters(memberId, type, location,
			startDate, endDate, pageable);
		List<Session> content = page.getContent().stream()
			.map(SessionMapper::toDomain)
			.toList();
		return PageResult.of(content, pageRequest, page.getTotalElements());
	}

	@Override
	public List<Session> findByYearMonth(YearMonth yearMonth, SessionType type, SessionLocation location,
		Long courseId, Long curriculumId) {
		LocalDate startOfMonth = yearMonth.atDay(1);
		LocalDate startOfNextMonth = yearMonth.plusMonths(1).atDay(1);

		Instant startOfMonthInstant = startOfMonth.atStartOfDay().toInstant(ZoneOffset.UTC);
		Instant startOfNextMonthInstant = startOfNextMonth.atStartOfDay().toInstant(ZoneOffset.UTC);

		return jpaRepository.findByYearMonth(startOfMonthInstant, startOfNextMonthInstant,
				type, location, courseId, curriculumId).stream()
			.map(SessionMapper::toDomain)
			.toList();
	}

	@Override
	public List<Long> findSessionIdsByPeriodAndFilters(Instant startDate, Instant endDate, Long courseId,
		Long curriculumId) {
		return jpaRepository.findIdsByPeriodAndFilters(startDate, endDate, courseId, curriculumId);
	}

	@Override
	public List<Long> findSessionIdsByCourseId(Long courseId) {
		return jpaRepository.findByCourseId(courseId).stream()
			.map(SessionEntity::getId)
			.toList();
	}

	@Override
	public List<Long> findSessionIdsByCurriculumId(Long curriculumId) {
		return jpaRepository.findByCurriculumId(curriculumId).stream()
			.map(SessionEntity::getId)
			.toList();
	}

	@Override
	public List<Long> findSessionIdsByMemberId(Long memberId) {
		return jpaRepository.findByMemberIdWithFilters(memberId, null, null, null, null, Pageable.unpaged())
			.getContent()
			.stream()
			.map(SessionEntity::getId)
			.toList();
	}

	@Override
	public List<Long> findSessionIdsByMonthAndFilters(int year, int month, Long courseId, Long curriculumId) {
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate startOfMonth = yearMonth.atDay(1);
		LocalDate startOfNextMonth = yearMonth.plusMonths(1).atDay(1);

		Instant startDate = startOfMonth.atStartOfDay().toInstant(ZoneOffset.UTC);
		Instant endDate = startOfNextMonth.atStartOfDay().toInstant(ZoneOffset.UTC);

		return findSessionIdsByPeriodAndFilters(startDate, endDate, courseId, curriculumId);
	}

	@Override
	public List<SessionInfo> findSessionInfoProjectionByIds(List<Long> sessionIds) {
		if (sessionIds.isEmpty()) {
			return List.of();
		}

		return jpaRepository.findSessionInfoProjectionByIds(sessionIds);
	}

	@Override
	public List<SessionInfo> findSessionInfoByIds(List<Long> sessionIds) {
		return findSessionInfoProjectionByIds(sessionIds);
	}

	@Override
	public Map<Long, SessionInfo> findSessionInfoMapByIds(List<Long> sessionIds) {
		return findSessionInfoByIds(sessionIds).stream()
			.collect(Collectors.toMap(SessionInfo::sessionId, info -> info));
	}

	private Pageable toSpringPageable(PageRequest pageRequest) {
		if (pageRequest.hasSort()) {
			Sort.Direction direction = pageRequest.sortOrder() == SortOrder.DESC
				? Sort.Direction.DESC : Sort.Direction.ASC;
			return org.springframework.data.domain.PageRequest.of(
				pageRequest.page(), pageRequest.size(), Sort.by(direction, pageRequest.sortBy()));
		}
		return org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size());
	}

}
