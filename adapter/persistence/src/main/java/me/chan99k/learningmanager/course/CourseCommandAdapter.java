package me.chan99k.learningmanager.course;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.course.entity.CourseEntity;
import me.chan99k.learningmanager.course.mapper.CourseMapper;

@Repository
public class CourseCommandAdapter implements CourseCommandRepository {
	private final JpaCourseRepository jpaCourseRepository;

	public CourseCommandAdapter(JpaCourseRepository jpaCourseRepository) {
		this.jpaCourseRepository = jpaCourseRepository;
	}

	@Override
	public Course create(Course course) {
		CourseEntity entity = CourseMapper.toEntity(course);
		CourseEntity saved = jpaCourseRepository.save(entity);
		return CourseMapper.toDomain(saved);
	}

	@Override
	public Course save(Course course) {
		CourseEntity entity = CourseMapper.toEntity(course);
		CourseEntity saved = jpaCourseRepository.save(entity);
		return CourseMapper.toDomain(saved);
	}

	@Override
	public void delete(Course course) {
		CourseEntity entity = CourseMapper.toEntity(course);
		jpaCourseRepository.delete(entity);
	}
}
