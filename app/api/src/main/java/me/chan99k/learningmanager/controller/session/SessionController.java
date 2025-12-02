package me.chan99k.learningmanager.controller.session;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.common.PageResult;
import me.chan99k.learningmanager.exception.DomainException;
import me.chan99k.learningmanager.session.Session;
import me.chan99k.learningmanager.session.SessionCreation;
import me.chan99k.learningmanager.session.SessionDeletion;
import me.chan99k.learningmanager.session.SessionDetailRetrieval;
import me.chan99k.learningmanager.session.SessionListRetrieval;
import me.chan99k.learningmanager.session.SessionLocation;
import me.chan99k.learningmanager.session.SessionProblemCode;
import me.chan99k.learningmanager.session.SessionQueryRepository;
import me.chan99k.learningmanager.session.SessionType;
import me.chan99k.learningmanager.session.SessionUpdate;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {
	private final SessionCreation sessionCreation;
	private final SessionUpdate sessionUpdate;
	private final SessionDeletion sessionDeletion;
	private final SessionListRetrieval sessionListRetrieval;
	private final SessionQueryRepository sessionQueryRepository;
	private final TaskExecutor sessionTaskExecutor;

	public SessionController(SessionCreation sessionCreation,
		SessionUpdate sessionUpdate,
		SessionDeletion sessionDeletion,
		SessionListRetrieval sessionListRetrieval,
		SessionQueryRepository sessionQueryRepository,
		TaskExecutor sessionTaskExecutor) {
		this.sessionCreation = sessionCreation;
		this.sessionUpdate = sessionUpdate;
		this.sessionDeletion = sessionDeletion;
		this.sessionListRetrieval = sessionListRetrieval;
		this.sessionQueryRepository = sessionQueryRepository;
		this.sessionTaskExecutor = sessionTaskExecutor;
	}

	@PostMapping
	public ResponseEntity<SessionCreation.Response> createSession(
		@Valid @RequestBody SessionCreation.Request request
	) {
		Session session = sessionCreation.createSession(request);

		SessionCreation.Response response = new SessionCreation.Response(
			session.getId(),
			session.getTitle(),
			session.getScheduledAt(),
			session.getScheduledEndAt(),
			session.getType(),
			session.getLocation(),
			session.getLocationDetails(),
			session.getCourseId(),
			session.getCurriculumId()
		);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{sessionId}")
	public ResponseEntity<Void> updateSession(
		@PathVariable Long sessionId,
		@Valid @RequestBody SessionUpdate.Request request) {
		sessionUpdate.updateSession(sessionId, request);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{sessionId}")
	public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
		sessionDeletion.deleteSession(sessionId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}")
	public CompletableFuture<ResponseEntity<SessionDetailRetrieval.Response>> getSession(
		@PathVariable Long id
	) {
		return CompletableFuture.supplyAsync(() -> {
			Session session = sessionQueryRepository.findById(id)
				.orElseThrow(() -> new DomainException(SessionProblemCode.SESSION_NOT_FOUND));

			SessionDetailRetrieval.Response response = new SessionDetailRetrieval.Response(
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
				session.getParticipants().size()
			);
			return ResponseEntity.ok(response);
		}, sessionTaskExecutor);
	}

	@GetMapping
	public CompletableFuture<ResponseEntity<PageResult<SessionListRetrieval.SessionListResponse>>> getSessionList(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(defaultValue = "scheduledAt,desc") String sort,
		@RequestParam(required = false) SessionType type,
		@RequestParam(required = false) SessionLocation location,
		@RequestParam(required = false) Instant startDate,
		@RequestParam(required = false) Instant endDate
	) {
		return CompletableFuture.supplyAsync(() -> {
			var request = new SessionListRetrieval.SessionListRequest(page, size, sort, type, location, startDate,
				endDate);
			PageResult<SessionListRetrieval.SessionListResponse> response = sessionListRetrieval.getSessionList(
				request);
			return ResponseEntity.ok(response);
		}, sessionTaskExecutor);
	}

	@GetMapping("/courses/{courseId}")
	public CompletableFuture<ResponseEntity<PageResult<SessionListRetrieval.SessionListResponse>>> getCourseSessionList(
		@PathVariable Long courseId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(defaultValue = "scheduledAt,desc") String sort,
		@RequestParam(required = false) SessionType type,
		@RequestParam(required = false) SessionLocation location,
		@RequestParam(required = false) Instant startDate,
		@RequestParam(required = false) Instant endDate,
		@RequestParam(defaultValue = "true") Boolean includeChildSessions
	) {
		return CompletableFuture.supplyAsync(() -> {
			var request = new SessionListRetrieval.CourseSessionListRequest(
				page, size, sort, type, location, startDate, endDate, includeChildSessions
			);
			PageResult<SessionListRetrieval.SessionListResponse> response =
				sessionListRetrieval.getCourseSessionList(courseId, request);
			return ResponseEntity.ok(response);
		}, sessionTaskExecutor);
	}

	@GetMapping("/curricula/{curriculumId}")
	public CompletableFuture<ResponseEntity<PageResult<SessionListRetrieval.SessionListResponse>>> getCurriculumSessionList(
		@PathVariable Long curriculumId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(defaultValue = "scheduledAt,desc") String sort,
		@RequestParam(required = false) SessionType type,
		@RequestParam(required = false) SessionLocation location,
		@RequestParam(required = false) Instant startDate,
		@RequestParam(required = false) Instant endDate,
		@RequestParam(defaultValue = "true") Boolean includeChildSessions
	) {
		return CompletableFuture.supplyAsync(() -> {
			var request = new SessionListRetrieval.CurriculumSessionListRequest(
				page, size, sort, type, location, startDate, endDate, includeChildSessions
			);
			PageResult<SessionListRetrieval.SessionListResponse> response =
				sessionListRetrieval.getCurriculumSessionList(curriculumId, request);
			return ResponseEntity.ok(response);
		}, sessionTaskExecutor);
	}

	@GetMapping("/members/{memberId}")
	public CompletableFuture<ResponseEntity<PageResult<SessionListRetrieval.SessionListResponse>>> getUserSessionList(
		@PathVariable Long memberId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(defaultValue = "scheduledAt,desc") String sort,
		@RequestParam(required = false) SessionType type,
		@RequestParam(required = false) SessionLocation location,
		@RequestParam(required = false) Instant startDate,
		@RequestParam(required = false) Instant endDate
	) {
		return CompletableFuture.supplyAsync(() -> {
			var request = new SessionListRetrieval.UserSessionListRequest(
				page, size, sort, type, location, startDate, endDate
			);
			PageResult<SessionListRetrieval.SessionListResponse> response =
				sessionListRetrieval.getUserSessionList(memberId, request);
			return ResponseEntity.ok(response);
		}, sessionTaskExecutor);
	}

	@GetMapping("/calendar")
	public CompletableFuture<ResponseEntity<Map<LocalDate, List<SessionListRetrieval.SessionCalendarResponse>>>> getSessionCalendar(
		@RequestParam int year,
		@RequestParam int month,
		@RequestParam(required = false) SessionType type,
		@RequestParam(required = false) SessionLocation location,
		@RequestParam(required = false) Long courseId,
		@RequestParam(required = false) Long curriculumId
	) {
		return CompletableFuture.supplyAsync(() -> {
			YearMonth yearMonth = YearMonth.of(year, month);
			var request = new SessionListRetrieval.SessionCalendarRequest(
				type, location, courseId, curriculumId
			);
			Map<LocalDate, List<SessionListRetrieval.SessionCalendarResponse>> response =
				sessionListRetrieval.getSessionCalendar(yearMonth, request);
			return ResponseEntity.ok(response);
		}, sessionTaskExecutor);
	}
}