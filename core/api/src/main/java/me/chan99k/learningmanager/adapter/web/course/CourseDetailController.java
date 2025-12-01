package me.chan99k.learningmanager.adapter.web.course;

import java.util.concurrent.CompletableFuture;

import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.application.course.CourseMemberInfo;
import me.chan99k.learningmanager.application.course.provides.CourseDetailRetrieval;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseDetailController {

	private final CourseDetailRetrieval courseDetailRetrieval;
	private final TaskExecutor courseTaskExecutor;

	public CourseDetailController(
		CourseDetailRetrieval courseDetailRetrieval,
		TaskExecutor courseTaskExecutor
	) {
		this.courseDetailRetrieval = courseDetailRetrieval;
		this.courseTaskExecutor = courseTaskExecutor;
	}

	@GetMapping("/{courseId}")
	public CompletableFuture<ResponseEntity<CourseDetailRetrieval.CourseDetailResponse>> getCourseDetail(
		@PathVariable Long courseId
	) {
		return CompletableFuture.supplyAsync(() -> {
			CourseDetailRetrieval.CourseDetailResponse response = courseDetailRetrieval.getCourseDetail(courseId);
			return ResponseEntity.ok(response);
		}, courseTaskExecutor);
	}

	@GetMapping("/{courseId}/members")
	public CompletableFuture<ResponseEntity<Page<CourseMemberInfo>>> getCourseMembers(
		@PathVariable Long courseId,
		@PageableDefault(size = 20) Pageable pageable
	) {
		return CompletableFuture.supplyAsync(() -> {
			Page<CourseMemberInfo> response = courseDetailRetrieval.getCourseMembers(courseId, pageable);
			return ResponseEntity.ok(response);
		}, courseTaskExecutor);
	}
}