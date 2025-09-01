package me.chan99k.learningmanager.adapter.persistence.course;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.course.requires.CourseCommandRepository;
import me.chan99k.learningmanager.domain.course.Course;

@Repository
public class CourseCommandAdapter implements CourseCommandRepository {
	private final JpaCourseRepository jpaCourseRepository;

	public CourseCommandAdapter(JpaCourseRepository jpaCourseRepository) {
		this.jpaCourseRepository = jpaCourseRepository;
	}

	@Override
	public Course create(Course course) {
		return jpaCourseRepository.save(course);
	}

	@Override
	public Course save(Course course) {
		return jpaCourseRepository.save(course);
	}
}
