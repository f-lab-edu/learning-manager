package me.chan99k.learningmanager.course;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.chan99k.learningmanager.course.entity.CourseEntity;

public interface JpaCourseRepository extends JpaRepository<CourseEntity, Long>, CustomCourseRepository {

	Optional<CourseEntity> findByTitle(String title);

}
