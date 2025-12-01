package me.chan99k.learningmanager.adapter.web.course;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.application.course.provides.CurriculumCreation;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseCurriculumAdditionController {

	private final CurriculumCreation curriculumCreation;

	public CourseCurriculumAdditionController(CurriculumCreation curriculumCreation) {
		this.curriculumCreation = curriculumCreation;

	}

	@PostMapping("/{courseId}/curriculums")
	public ResponseEntity<CurriculumCreation.Response> addCourseCurriculum(
		@PathVariable Long courseId,
		@Valid @RequestBody CurriculumCreation.Request request
	) {
		CurriculumCreation.Response response = curriculumCreation.createCurriculum(courseId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
