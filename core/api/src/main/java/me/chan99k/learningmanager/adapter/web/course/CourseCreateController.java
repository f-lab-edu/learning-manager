package me.chan99k.learningmanager.adapter.web.course;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.application.course.CourseCreationService;
import me.chan99k.learningmanager.application.course.provides.CourseCreation;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseCreateController {

	private final CourseCreationService courseCreationService;

	public CourseCreateController(CourseCreationService courseCreationService) {
		this.courseCreationService = courseCreationService;
	}

	@PostMapping
	public ResponseEntity<CourseCreation.Response> createCourse(
		@Valid @RequestBody CourseCreation.Request request
	) {
		CourseCreation.Response response = courseCreationService.createCourse(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
