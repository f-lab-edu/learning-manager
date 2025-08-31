package me.chan99k.learningmanager.adapter.web.course;

import java.util.concurrent.CompletableFuture;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.application.course.CourseMemberService;
import me.chan99k.learningmanager.application.course.provides.CourseMemberAddition;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseMemberController {
	private final CourseMemberService courseMemberService;
	private final AsyncTaskExecutor courseTaskExecutor;

	public CourseMemberController(CourseMemberService courseMemberService, AsyncTaskExecutor courseTaskExecutor) {
		this.courseMemberService = courseMemberService;
		this.courseTaskExecutor = courseTaskExecutor;
	}

	@PostMapping("/{courseId}/members")
	public CompletableFuture<ResponseEntity<Void>> addMemberToCourse(
		@PathVariable Long courseId,
		@Valid @RequestBody CourseMemberAddition.Request request
	) {
		return CompletableFuture.runAsync(() -> {
			courseMemberService.addMemberToCourse(courseId, request);
		}, courseTaskExecutor).thenApply(v -> ResponseEntity.ok().build());
	}
}
