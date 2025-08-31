package me.chan99k.learningmanager.adapter.persistence.course;

import org.springframework.data.jpa.repository.JpaRepository;

import me.chan99k.learningmanager.domain.course.Course;

public interface JpaCourseRepository extends JpaRepository<Course, Long> {
}
