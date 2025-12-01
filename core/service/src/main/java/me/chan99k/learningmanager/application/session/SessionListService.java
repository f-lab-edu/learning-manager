package me.chan99k.learningmanager.application.session;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.application.session.provides.SessionListRetrieval;
import me.chan99k.learningmanager.application.session.requires.SessionQueryRepository;
import me.chan99k.learningmanager.domain.session.Session;

@Service
@Transactional(readOnly = true)
public class SessionListService implements SessionListRetrieval {

	private final SessionQueryRepository sessionQueryRepository;
	private final Clock clock;

	public SessionListService(SessionQueryRepository sessionQueryRepository, Clock clock) {
		this.sessionQueryRepository = sessionQueryRepository;
		this.clock = clock;
	}

	@Override
	public Page<SessionListResponse> getSessionList(SessionListRequest request) {
		Pageable pageable = createPageable(request.page(), request.size(), request.sort());

		Page<Session> sessions = sessionQueryRepository.findAllWithFilters(
			request.type(),
			request.location(),
			request.startDate(),
			request.endDate(),
			pageable
		);

		return sessions.map(this::toSessionListResponse);
	}

	@Override
	public Page<SessionListResponse> getCourseSessionList(Long courseId, CourseSessionListRequest request) {
		Pageable pageable = createPageable(request.page(), request.size(), request.sort());

		Page<Session> sessions = sessionQueryRepository.findByCourseIdWithFilters(
			courseId,
			request.type(),
			request.location(),
			request.startDate(),
			request.endDate(),
			request.includeChildSessions(),
			pageable
		);

		return sessions.map(this::toSessionListResponse);
	}

	@Override
	public Page<SessionListResponse> getCurriculumSessionList(Long curriculumId, CurriculumSessionListRequest request) {
		Pageable pageable = createPageable(request.page(), request.size(), request.sort());

		Page<Session> sessions = sessionQueryRepository.findByCurriculumIdWithFilters(
			curriculumId,
			request.type(),
			request.location(),
			request.startDate(),
			request.endDate(),
			request.includeChildSessions(),
			pageable
		);

		return sessions.map(this::toSessionListResponse);
	}

	@Override
	public Page<SessionListResponse> getUserSessionList(Long memberId, UserSessionListRequest request) {
		Pageable pageable = createPageable(request.page(), request.size(), request.sort());

		Page<Session> sessions = sessionQueryRepository.findByMemberIdWithFilters(
			memberId,
			request.type(),
			request.location(),
			request.startDate(),
			request.endDate(),
			pageable
		);

		return sessions.map(this::toSessionListResponse);
	}

	@Override
	public Map<LocalDate, List<SessionCalendarResponse>> getSessionCalendar(YearMonth yearMonth,
		SessionCalendarRequest request) {
		List<Session> sessions = sessionQueryRepository.findByYearMonth(
			yearMonth,
			request.type(),
			request.location(),
			request.courseId(),
			request.curriculumId()
		);

		return sessions.stream()
			.collect(Collectors.groupingBy(
				session -> session.getScheduledAt().atZone(clock.getZone()).toLocalDate(),
				Collectors.mapping(this::toSessionCalendarResponse, Collectors.toList())
			));
	}

	private Pageable createPageable(int page, int size, String sort) {
		if (sort == null || sort.isEmpty()) {
			return PageRequest.of(page, size);
		}

		String[] sortParts = sort.split(",");
		String property = sortParts[0];
		Sort.Direction direction = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1])
			? Sort.Direction.ASC
			: Sort.Direction.DESC;

		return PageRequest.of(page, size, Sort.by(direction, property));
	}

	private SessionListResponse toSessionListResponse(Session session) {
		return new SessionListResponse(
			session.getId(),
			session.getTitle(),
			session.getScheduledAt(),
			session.getScheduledEndAt(),
			session.getType(),
			session.getLocation(),
			session.getLocationDetails(),
			session.getCourseId(),
			session.getCurriculumId(),
			session.getParent() != null ? session.getParent().getId() : null,
			session.getChildren().size(),
			session.getParticipants().size(),
			determineSessionStatus(session.getScheduledAt(), session.getScheduledEndAt())
		);
	}

	private SessionStatus determineSessionStatus(Instant scheduledAt, Instant scheduledEndAt) {
		Instant now = clock.instant();

		if (now.isBefore(scheduledAt)) {
			return SessionStatus.UPCOMING;
		} else if (now.isAfter(scheduledEndAt)) {
			return SessionStatus.COMPLETED;
		} else {
			return SessionStatus.ONGOING;
		}
	}

	private SessionCalendarResponse toSessionCalendarResponse(Session session) {
		return new SessionCalendarResponse(
			session.getId(),
			session.getTitle(),
			session.getScheduledAt(),
			session.getScheduledEndAt(),
			session.getType(),
			session.getLocation(),
			session.getLocationDetails(),
			session.getCourseId(),
			session.getCurriculumId(),
			determineSessionStatus(session.getScheduledAt(), session.getScheduledEndAt())
		);
	}
}