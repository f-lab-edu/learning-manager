package me.chan99k.learningmanager.web.course;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.chan99k.learningmanager.application.course.provides.CourseInfoUpdate;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseInfoUpdateController {

	private final CourseInfoUpdate courseInfoUpdate;

	public CourseInfoUpdateController(CourseInfoUpdate courseInfoUpdate) {
		this.courseInfoUpdate = courseInfoUpdate;

	}

	@PutMapping("/{courseId}")
	public ResponseEntity<Void> updateCourse(
		@PathVariable Long courseId,
		@RequestBody CourseInfoUpdate.Request request
	) {
		courseInfoUpdate.updateCourseInfo(courseId, request);
		return ResponseEntity.ok().build();
	}
}