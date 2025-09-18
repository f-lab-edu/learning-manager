package me.chan99k.learningmanager.adapter.web.course;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.application.session.provides.SessionListRetrieval;
import me.chan99k.learningmanager.domain.session.SessionLocation;
import me.chan99k.learningmanager.domain.session.SessionType;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseCalendarController {

	private final SessionListRetrieval sessionListRetrieval;
	private final TaskExecutor courseTaskExecutor;

	public CourseCalendarController(SessionListRetrieval sessionListRetrieval, TaskExecutor courseTaskExecutor) {
		this.sessionListRetrieval = sessionListRetrieval;
		this.courseTaskExecutor = courseTaskExecutor;
	}

	@GetMapping("/{courseId}/calendar")
	public CompletableFuture<ResponseEntity<Map<LocalDate, List<SessionListRetrieval.SessionCalendarResponse>>>> getCourseCalendar(
		@PathVariable Long courseId,
		@RequestParam int year,
		@RequestParam int month,
		@RequestParam(required = false) SessionType type,
		@RequestParam(required = false) SessionLocation location
	) {
		return CompletableFuture.supplyAsync(() -> {
			YearMonth yearMonth = YearMonth.of(year, month);
			var request = new SessionListRetrieval.SessionCalendarRequest(
				type, location, courseId, null
			);
			Map<LocalDate, List<SessionListRetrieval.SessionCalendarResponse>> response =
				sessionListRetrieval.getSessionCalendar(yearMonth, request);
			return ResponseEntity.ok(response);
		}, courseTaskExecutor);
	}

	@GetMapping("/{courseId}/curricula/{curriculumId}/calendar")
	public CompletableFuture<ResponseEntity<Map<LocalDate, List<SessionListRetrieval.SessionCalendarResponse>>>> getCurriculumCalendar(
		@PathVariable Long courseId,
		@PathVariable Long curriculumId,
		@RequestParam int year,
		@RequestParam int month,
		@RequestParam(required = false) SessionType type,
		@RequestParam(required = false) SessionLocation location
	) {
		return CompletableFuture.supplyAsync(() -> {
			YearMonth yearMonth = YearMonth.of(year, month);
			var request = new SessionListRetrieval.SessionCalendarRequest(
				type, location, courseId, curriculumId
			);
			Map<LocalDate, List<SessionListRetrieval.SessionCalendarResponse>> response =
				sessionListRetrieval.getSessionCalendar(yearMonth, request);
			return ResponseEntity.ok(response);
		}, courseTaskExecutor);
	}
}