package me.chan99k.learningmanager.controller.course;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.course.CourseDeletion;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseDeletionController {

	private final CourseDeletion courseDeletion;

	public CourseDeletionController(CourseDeletion courseDeletion) {
		this.courseDeletion = courseDeletion;
	}

	@DeleteMapping("/{courseId}")
	public ResponseEntity<Void> deleteCourse(
		@PathVariable Long courseId
	) {
		courseDeletion.deleteCourse(courseId);
		return ResponseEntity.noContent().build();
	}
}