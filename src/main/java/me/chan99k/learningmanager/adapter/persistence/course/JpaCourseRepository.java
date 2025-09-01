package me.chan99k.learningmanager.adapter.persistence.course;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.chan99k.learningmanager.domain.course.Course;

public interface JpaCourseRepository extends JpaRepository<Course, Long> {
	Optional<Course> findByTitle(String title);

	@Query("SELECT CASE WHEN COUNT(cm) > 0 THEN true ELSE false END " +
		"FROM CourseMember cm " +
		"WHERE cm.course.id = :courseId AND cm.memberId = :memberId AND cm.courseRole = 'MANAGER'")
	boolean isCourseManager(@Param("courseId") Long courseId, @Param("memberId") Long memberId);
}
