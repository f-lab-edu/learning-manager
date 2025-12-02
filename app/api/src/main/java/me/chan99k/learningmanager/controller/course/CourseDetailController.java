package me.chan99k.learningmanager.controller.course;

import java.util.concurrent.CompletableFuture;

import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.common.PageRequest;
import me.chan99k.learningmanager.common.PageResult;
import me.chan99k.learningmanager.course.CourseDetailRetrieval;
import me.chan99k.learningmanager.course.CourseMemberInfo;

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
	public CompletableFuture<ResponseEntity<PageResult<CourseMemberInfo>>> getCourseMembers(
		@PathVariable Long courseId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		return CompletableFuture.supplyAsync(() -> {
			PageRequest pageRequest = PageRequest.of(page, size);
			PageResult<CourseMemberInfo> response = courseDetailRetrieval.getCourseMembers(courseId, pageRequest);
			return ResponseEntity.ok(response);
		}, courseTaskExecutor);
	}
}