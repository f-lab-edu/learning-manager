package me.chan99k.learningmanager.adapter.web.course;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.application.course.provides.CourseInfoUpdate;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseInfoUpdateController {

	private final CourseInfoUpdate courseInfoUpdate;
	private final Executor courseTaskExecutor;

	public CourseInfoUpdateController(
		CourseInfoUpdate courseInfoUpdate,
		Executor courseTaskExecutor) {
		this.courseInfoUpdate = courseInfoUpdate;
		this.courseTaskExecutor = courseTaskExecutor;
	}

	@PutMapping("/{courseId}")
	public CompletableFuture<ResponseEntity<Void>> updateCourse(
		@PathVariable Long courseId,
		@RequestBody CourseInfoUpdate.Request request) {
		return CompletableFuture.supplyAsync(() -> {
			courseInfoUpdate.updateCourseInfo(courseId, request);
			return ResponseEntity.ok().build();
		}, courseTaskExecutor);
	}
}