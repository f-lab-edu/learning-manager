package me.chan99k.learningmanager.course;

public interface CourseCommandRepository {
	Course create(Course course);

	Course save(Course course);

	void delete(Course course);
}
