package me.chan99k.learningmanager.session;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.chan99k.learningmanager.common.PageRequest;
import me.chan99k.learningmanager.common.PageResult;
import me.chan99k.learningmanager.common.SortOrder;

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
	public PageResult<SessionListResponse> getSessionList(SessionListRequest request) {
		PageRequest pageRequest = createPageRequest(request.page(), request.size(), request.sort());

		PageResult<Session> sessions = sessionQueryRepository.findAllWithFilters(
			request.type(),
			request.location(),
			request.startDate(),
			request.endDate(),
			pageRequest
		);

		return sessions.map(this::toSessionListResponse);
	}

	@Override
	public PageResult<SessionListResponse> getCourseSessionList(Long courseId, CourseSessionListRequest request) {
		PageRequest pageRequest = createPageRequest(request.page(), request.size(), request.sort());

		PageResult<Session> sessions = sessionQueryRepository.findByCourseIdWithFilters(
			courseId,
			request.type(),
			request.location(),
			request.startDate(),
			request.endDate(),
			request.includeChildSessions(),
			pageRequest
		);

		return sessions.map(this::toSessionListResponse);
	}

	@Override
	public PageResult<SessionListResponse> getCurriculumSessionList(Long curriculumId,
		CurriculumSessionListRequest request) {
		PageRequest pageRequest = createPageRequest(request.page(), request.size(), request.sort());

		PageResult<Session> sessions = sessionQueryRepository.findByCurriculumIdWithFilters(
			curriculumId,
			request.type(),
			request.location(),
			request.startDate(),
			request.endDate(),
			request.includeChildSessions(),
			pageRequest
		);

		return sessions.map(this::toSessionListResponse);
	}

	@Override
	public PageResult<SessionListResponse> getUserSessionList(Long memberId, UserSessionListRequest request) {
		PageRequest pageRequest = createPageRequest(request.page(), request.size(), request.sort());

		PageResult<Session> sessions = sessionQueryRepository.findByMemberIdWithFilters(
			memberId,
			request.type(),
			request.location(),
			request.startDate(),
			request.endDate(),
			pageRequest
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

	private PageRequest createPageRequest(int page, int size, String sort) {
		if (sort == null || sort.isEmpty()) {
			return PageRequest.of(page, size);
		}

		String[] sortParts = sort.split(",");
		String sortBy = sortParts[0];
		SortOrder sortOrder = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1])
			? SortOrder.ASC
			: SortOrder.DESC;

		return PageRequest.of(page, size, sortBy, sortOrder);
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