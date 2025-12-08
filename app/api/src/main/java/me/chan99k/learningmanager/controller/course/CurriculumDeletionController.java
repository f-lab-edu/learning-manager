package me.chan99k.learningmanager.controller.course;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.course.CurriculumDeletion;
import me.chan99k.learningmanager.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/courses")
public class CurriculumDeletionController {

	private final CurriculumDeletion curriculumDeletion;

	public CurriculumDeletionController(CurriculumDeletion curriculumDeletion) {
		this.curriculumDeletion = curriculumDeletion;
	}

	@PreAuthorize("@courseSecurity.isManager(#courseId, #user.memberId)")
	@DeleteMapping("/{courseId}/curriculums/{curriculumId}")
	public ResponseEntity<Void> deleteCurriculum(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long courseId,
		@PathVariable Long curriculumId
	) {
		curriculumDeletion.deleteCurriculum(user.getMemberId(), courseId, curriculumId);
		return ResponseEntity.noContent().build();
	}
}