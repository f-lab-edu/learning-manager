package me.chan99k.learningmanager.adapter.web.course;

import java.util.concurrent.CompletableFuture;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.application.course.provides.CourseDeletion;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseDeletionController {
	private final CourseDeletion courseDeletion;
	private final AsyncTaskExecutor courseTaskExecutor;

	public CourseDeletionController(CourseDeletion courseDeletion,
		AsyncTaskExecutor courseTaskExecutor) {
		this.courseDeletion = courseDeletion;
		this.courseTaskExecutor = courseTaskExecutor;
	}

	@DeleteMapping("/{courseId}")
	public CompletableFuture<ResponseEntity<Void>> deleteCourse(
		@PathVariable Long courseId
	) {
		return CompletableFuture.supplyAsync(() -> {
			courseDeletion.deleteCourse(courseId);
			return ResponseEntity.ok().build();
		}, courseTaskExecutor);
	}
}