package me.chan99k.learningmanager.controller.course;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.course.CourseDeletion;
import me.chan99k.learningmanager.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseDeletionController {

	private final CourseDeletion courseDeletion;

	public CourseDeletionController(CourseDeletion courseDeletion) {
		this.courseDeletion = courseDeletion;
	}

	@PreAuthorize("@courseSecurity.isManager(#courseId, #user.memberId)")
	@DeleteMapping("/{courseId}")
	public ResponseEntity<Void> deleteCourse(
		@AuthenticationPrincipal CustomUserDetails user,
		@PathVariable Long courseId
	) {

		courseDeletion.deleteCourse(user.getMemberId(), courseId);
		return ResponseEntity.noContent().build();
	}
}