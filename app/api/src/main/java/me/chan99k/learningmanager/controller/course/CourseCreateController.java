package me.chan99k.learningmanager.controller.course;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.chan99k.learningmanager.course.CourseCreation;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseCreateController {

	private final CourseCreation courseCreation;

	public CourseCreateController(CourseCreation courseCreation) {
		this.courseCreation = courseCreation;
	}

	@PostMapping
	public ResponseEntity<CourseCreation.Response> createCourse(
		@Valid @RequestBody CourseCreation.Request request
	) {
		CourseCreation.Response response = courseCreation.createCourse(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
