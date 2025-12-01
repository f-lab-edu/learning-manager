package me.chan99k.learningmanager.adapter.web.course;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.application.course.provides.CurriculumInfoUpdate;

@RestController
@RequestMapping("/api/v1/courses")
public class CurriculumUpdateController {

	private final CurriculumInfoUpdate curriculumInfoUpdate;

	public CurriculumUpdateController(
		CurriculumInfoUpdate curriculumInfoUpdate) {
		this.curriculumInfoUpdate = curriculumInfoUpdate;
	}

	@PutMapping("/{courseId}/curriculums/{curriculumId}")
	public ResponseEntity<Void> updateCurriculum(
		@PathVariable Long courseId,
		@PathVariable Long curriculumId,
		@RequestBody CurriculumInfoUpdate.Request request
	) {
		curriculumInfoUpdate.updateCurriculumInfo(courseId, curriculumId, request);
		return ResponseEntity.ok().build();
	}
}