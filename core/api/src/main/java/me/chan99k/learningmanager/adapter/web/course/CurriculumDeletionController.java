package me.chan99k.learningmanager.adapter.web.course;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.application.course.provides.CurriculumDeletion;

@RestController
@RequestMapping("/api/v1/courses")
public class CurriculumDeletionController {

	private final CurriculumDeletion curriculumDeletion;

	public CurriculumDeletionController(CurriculumDeletion curriculumDeletion) {
		this.curriculumDeletion = curriculumDeletion;
	}

	@DeleteMapping("/{courseId}/curriculums/{curriculumId}")
	public ResponseEntity<Void> deleteCurriculum(
		@PathVariable Long courseId,
		@PathVariable Long curriculumId
	) {
		curriculumDeletion.deleteCurriculum(courseId, curriculumId);
		return ResponseEntity.noContent().build();
	}
}