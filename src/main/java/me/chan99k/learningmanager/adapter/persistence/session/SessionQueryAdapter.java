package me.chan99k.learningmanager.adapter.persistence.session;

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
import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.session.requires.SessionQueryRepository;
import me.chan99k.learningmanager.domain.course.CourseRole;
import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionLocation;
import me.chan99k.learningmanager.domain.session.SessionType;

@Repository
public class SessionQueryAdapter implements SessionQueryRepository {
	private final SessionJpaRepository jpaRepository;

	public SessionQueryAdapter(SessionJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Optional<Session> findById(Long sessionId) {
		return jpaRepository.findById(sessionId);
	}

	@Override
	public List<Session> findByCourseId(Long courseId) {
		return jpaRepository.findByCourseId(courseId);
	}

	@Override
	public List<Session> findByCurriculumId(Long curriculumId) {
		return jpaRepository.findByCurriculumId(curriculumId);
	}

	@Override
	public List<Session> findByParentId(Long parentId) {
		return jpaRepository.findByParentId(parentId);
	}

	@Override
	public Optional<Session> findManagedSessionById(Long sessionId, Long memberId) {
		return jpaRepository.findManagedSessionById(sessionId, memberId, CourseRole.MANAGER);
	}

	@Override
	public Page<Session> findAllWithFilters(SessionType type, SessionLocation location,
		Instant startDate, Instant endDate, Pageable pageable) {
		return jpaRepository.findAllWithFilters(type, location, startDate, endDate, pageable);
	}

	@Override
	public Page<Session> findByCourseIdWithFilters(Long courseId, SessionType type,
		SessionLocation location, Instant startDate, Instant endDate,
		Boolean includeChildSessions, Pageable pageable) {
		return jpaRepository.findByCourseIdWithFilters(courseId, type, location,
			startDate, endDate, includeChildSessions, pageable);
	}

	@Override
	public Page<Session> findByCurriculumIdWithFilters(Long curriculumId, SessionType type,
		SessionLocation location, Instant startDate, Instant endDate,
		Boolean includeChildSessions, Pageable pageable) {
		return jpaRepository.findByCurriculumIdWithFilters(curriculumId, type, location,
			startDate, endDate, includeChildSessions, pageable);
	}

	@Override
	public Page<Session> findByMemberIdWithFilters(Long memberId, SessionType type,
		SessionLocation location, Instant startDate, Instant endDate, Pageable pageable) {
		return jpaRepository.findByMemberIdWithFilters(memberId, type, location,
			startDate, endDate, pageable);
	}

	@Override
	public List<Session> findByYearMonth(YearMonth yearMonth, SessionType type, SessionLocation location,
		Long courseId, Long curriculumId) {
		LocalDate startOfMonth = yearMonth.atDay(1);
		LocalDate startOfNextMonth = yearMonth.plusMonths(1).atDay(1);

		Instant startOfMonthInstant = startOfMonth.atStartOfDay().toInstant(ZoneOffset.UTC);
		Instant startOfNextMonthInstant = startOfNextMonth.atStartOfDay().toInstant(ZoneOffset.UTC);

		return jpaRepository.findByYearMonth(startOfMonthInstant, startOfNextMonthInstant,
			type, location, courseId, curriculumId);
	}

	@Override
	public List<Long> findSessionIdsByPeriodAndFilters(Instant startDate, Instant endDate, Long courseId,
		Long curriculumId) {
		// TODO: JpaRepository에 메서드 추가 필요
		// 임시 구현: 전체 세션을 조회한 후 필터링 (성능상 비효율적)
		return jpaRepository.findAll().stream()
			.filter(
				session -> session.getScheduledAt().isAfter(startDate) && session.getScheduledAt().isBefore(endDate))
			.filter(session -> courseId == null || courseId.equals(session.getCourseId()))
			.filter(session -> curriculumId == null || curriculumId.equals(session.getCurriculumId()))
			.map(Session::getId)
			.toList();
	}

	@Override
	public List<Long> findSessionIdsByCourseId(Long courseId) {
		return jpaRepository.findByCourseId(courseId).stream()
			.map(Session::getId)
			.toList();
	}

	@Override
	public List<Long> findSessionIdsByCurriculumId(Long curriculumId) {
		return jpaRepository.findByCurriculumId(curriculumId).stream()
			.map(Session::getId)
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
	public List<SessionInfo> findSessionInfoByIds(List<Long> sessionIds) {
		if (sessionIds.isEmpty()) {
			return List.of();
		}

		return jpaRepository.findAllById(sessionIds).stream()
			.map(this::toSessionInfo)
			.toList();
	}

	@Override
	public Map<Long, SessionInfo> findSessionInfoMapByIds(List<Long> sessionIds) {
		return findSessionInfoByIds(sessionIds).stream()
			.collect(Collectors.toMap(SessionInfo::sessionId, info -> info));
	}

	private SessionInfo toSessionInfo(Session session) {
		// TODO: Course, Curriculum 정보를 조인으로 가져와야 함
		// 현재는 임시 구현
		return new SessionInfo(
			session.getId(),
			session.getTitle(),
			session.getScheduledAt(),
			session.getCourseId(),
			"Course Title", // TODO: 실제 Course 제목 조회 필요
			session.getCurriculumId(),
			"Curriculum Title" // TODO: 실제 Curriculum 제목 조회 필요
		);
	}
}
