package me.chan99k.learningmanager.adapter.persistence.course;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.course.requires.CourseQueryRepository;
import me.chan99k.learningmanager.domain.course.Course;

@Repository

public class CourseQueryAdapter implements CourseQueryRepository {

	private final JpaCourseRepository jpaCourseRepository;

	public CourseQueryAdapter(JpaCourseRepository jpaCourseRepository) {
		this.jpaCourseRepository = jpaCourseRepository;
	}

	@Override
	public Optional<Course> findById(Long courseId) {
		return jpaCourseRepository.findById(courseId);
	}

	@Override
	public Optional<Course> findByTitle(String title) {
		return jpaCourseRepository.findByTitle(title);
	}

	@Override
	public boolean isCourseManager(Long courseId, Long memberId) {
		return jpaCourseRepository.isCourseManager(courseId, memberId);
	}
}
