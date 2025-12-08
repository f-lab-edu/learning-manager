package me.chan99k.learningmanager.course;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.chan99k.learningmanager.course.entity.CourseEntity;
import me.chan99k.learningmanager.member.CourseParticipationInfo;

public interface JpaCourseRepository extends JpaRepository<CourseEntity, Long> {
	Optional<CourseEntity> findByTitle(String title);

	@Query("SELECT c FROM CourseEntity c JOIN c.courseMemberList cm " +
		"WHERE c.id = :courseId AND cm.memberId = :memberId AND cm.courseRole = 'MANAGER'")
	Optional<CourseEntity> findManagedCourseById(@Param("courseId") Long courseId,
		@Param("memberId") Long memberId);

	@Query("SELECT c FROM CourseEntity c JOIN c.courseMemberList cm " +
		"WHERE cm.memberId = :memberId AND cm.courseRole = 'MANAGER'")
	List<CourseEntity> findManagedCoursesByMemberId(@Param("memberId") Long memberId);

	@Query("SELECT c FROM CourseEntity c JOIN c.courseMemberList cm " +
		"WHERE cm.memberId = :memberId")
	List<CourseEntity> findParticipatingCoursesByMemberId(@Param("memberId") Long memberId);

	@Query("SELECT new me.chan99k.learningmanager.member.CourseParticipationInfo(" +
		"c.id, c.title, c.description, cm.courseRole) " +
		"FROM CourseEntity c JOIN c.courseMemberList cm " +
		"WHERE cm.memberId = :memberId")
	List<CourseParticipationInfo> findParticipatingCoursesWithRoleByMemberId(@Param("memberId") Long memberId);

	@Query("""
		SELECT new me.chan99k.learningmanager.course.CourseDetailInfo(
		    c.id,
		    c.title,
		    c.description,
		    c.createdAt,
		    COUNT(DISTINCT cm.id),
		    COUNT(DISTINCT cur.id)
		)
		FROM CourseEntity c
		LEFT JOIN c.courseMemberList cm
		LEFT JOIN c.curriculumList cur
		WHERE c.id = :courseId
		GROUP BY c.id, c.title, c.description, c.createdAt
		""")
	Optional<CourseDetailInfo> findCourseBasicDetailsById(Long courseId);

	@Query("SELECT new me.chan99k.learningmanager.course.CourseMemberInfo(" +
		"cm.memberId, m.nickname, a.email, cm.courseRole, cm.createdAt) " +
		"FROM CourseMemberEntity cm " +
		"JOIN MemberEntity m ON cm.memberId = m.id " +
		"JOIN AccountEntity a ON a.member.id = m.id " +
		"WHERE cm.course.id = :courseId " +
		"ORDER BY cm.createdAt DESC")
	Page<CourseMemberInfo> findCourseMembersByCourseId(@Param("courseId") Long courseId, Pageable pageable);

	// === 인가(Authorization) 관련 쿼리 ===

	@Query("SELECT COUNT(cm) > 0 FROM CourseMemberEntity cm " +
		"WHERE cm.memberId = :memberId AND cm.course.id = :courseId AND cm.courseRole = :role")
	boolean existsByMemberIdAndCourseIdAndRole(
		@Param("memberId") Long memberId,
		@Param("courseId") Long courseId,
		@Param("role") CourseRole role
	);

	@Query("SELECT COUNT(cm) > 0 FROM CourseMemberEntity cm " +
		"WHERE cm.memberId = :memberId AND cm.course.id = :courseId AND cm.courseRole IN :roles")
	boolean existsByMemberIdAndCourseIdAndRoleIn(
		@Param("memberId") Long memberId,
		@Param("courseId") Long courseId,
		@Param("roles") List<CourseRole> roles
	);

	@Query("SELECT COUNT(cm) > 0 FROM CourseMemberEntity cm " +
		"WHERE cm.memberId = :memberId AND cm.course.id = :courseId")
	boolean existsByMemberIdAndCourseId(
		@Param("memberId") Long memberId,
		@Param("courseId") Long courseId
	);
}
