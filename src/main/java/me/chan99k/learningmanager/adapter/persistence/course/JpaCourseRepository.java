package me.chan99k.learningmanager.adapter.persistence.course;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.chan99k.learningmanager.application.course.CourseDetailInfo;
import me.chan99k.learningmanager.application.course.CourseMemberInfo;
import me.chan99k.learningmanager.application.member.CourseParticipationInfo;
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

	@Query("SELECT c FROM Course c JOIN c.courseMemberList cm " +
		"WHERE cm.memberId = :memberId")
	List<Course> findParticipatingCoursesByMemberId(@Param("memberId") Long memberId);

	@Query("SELECT new me.chan99k.learningmanager.application.member.CourseParticipationInfo(" +
		"c.id, c.title, c.description, cm.courseRole) " +
		"FROM Course c JOIN c.courseMemberList cm " +
		"WHERE cm.memberId = :memberId")
	List<CourseParticipationInfo> findParticipatingCoursesWithRoleByMemberId(@Param("memberId") Long memberId);

	@Query("""
		SELECT new me.chan99k.learningmanager.application.course.CourseDetailInfo(
		    c.id,
		    c.title,
		    c.description,
		    c.createdAt,
		    COUNT(DISTINCT cm.id),
		    COUNT(DISTINCT cur.id)
		)
		FROM Course c
		LEFT JOIN c.courseMemberList cm
		LEFT JOIN c.curriculumList cur
		WHERE c.id = :courseId
		GROUP BY c.id, c.title, c.description, c.createdAt
		""")
	Optional<CourseDetailInfo> findCourseBasicDetailsById(Long courseId);

	@Query("SELECT new me.chan99k.learningmanager.application.course.CourseMemberInfo(" +
		"cm.memberId, m.nickname.value, a.email.address, cm.courseRole, cm.createdAt) " +
		"FROM CourseMember cm " +
		"JOIN Member m ON cm.memberId = m.id " +
		"JOIN Account a ON a.member.id = m.id " +
		"WHERE cm.course.id = :courseId " +
		"ORDER BY cm.createdAt DESC")
	Page<CourseMemberInfo> findCourseMembersByCourseId(@Param("courseId") Long courseId, Pageable pageable);
}
