package me.chan99k.learningmanager.adapter.persistence.course;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.chan99k.learningmanager.domain.course.Course;

public interface JpaCourseRepository extends JpaRepository<Course, Long> {
	Optional<Course> findByTitle(String title);

	@Query("SELECT c FROM Course c JOIN c.courseMemberList cm " +
		"WHERE c.id = :courseId AND cm.memberId = :memberId AND cm.courseRole = 'MANAGER'")
	Optional<Course> findManagedCourseById(@Param("courseId") Long courseId,
		@Param("memberId") Long memberId);

	@Query("SELECT c FROM Course c JOIN c.courseMemberList cm " +
		"WHERE cm.memberId = :memberId AND cm.courseRole = 'MANAGER'")
	List<Course> findManagedCoursesByMemberId(@Param("memberId") Long memberId);
}
