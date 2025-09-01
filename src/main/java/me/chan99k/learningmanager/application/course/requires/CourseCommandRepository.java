package me.chan99k.learningmanager.application.course.requires;

import me.chan99k.learningmanager.domain.course.Course;

public interface CourseCommandRepository {
	Course create(Course course);

	Course save(Course course);

	void delete(Course course);
}
