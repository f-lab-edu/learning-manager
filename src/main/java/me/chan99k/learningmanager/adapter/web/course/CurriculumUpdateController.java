package me.chan99k.learningmanager.adapter.web.course;

import java.util.concurrent.CompletableFuture;

import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.application.course.provides.CurriculumInfoUpdate;

@RestController
@RequestMapping("/api/v1/courses")
public class CurriculumUpdateController {

	private final CurriculumInfoUpdate curriculumInfoUpdate;
	private final TaskExecutor courseTaskExecutor;

	public CurriculumUpdateController(
		CurriculumInfoUpdate curriculumInfoUpdate,
		TaskExecutor courseTaskExecutor) {
		this.curriculumInfoUpdate = curriculumInfoUpdate;
		this.courseTaskExecutor = courseTaskExecutor;
	}

	@PutMapping("/{courseId}/curriculums/{curriculumId}")
	public CompletableFuture<ResponseEntity<Void>> updateCurriculum(
		@PathVariable Long courseId,
		@PathVariable Long curriculumId,
		@Valid @RequestBody CurriculumInfoUpdate.Request request) {
		return CompletableFuture.supplyAsync(() -> {
			curriculumInfoUpdate.updateCurriculumInfo(courseId, curriculumId, request);
			return ResponseEntity.ok().build();
		}, courseTaskExecutor);
	}
}