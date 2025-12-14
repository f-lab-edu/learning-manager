package me.chan99k.learningmanager.course;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import me.chan99k.learningmanager.course.entity.CourseEntity;
import me.chan99k.learningmanager.member.CourseParticipationInfo;

public interface CustomCourseRepository {

	Page<CourseMemberInfo> findCourseMembersByCourseId(Long courseId, Pageable pageable);

	//  === Entity 조회  ===
	Optional<CourseEntity> findManagedCourseById(Long courseId, Long memberId);

	List<CourseEntity> findManagedCoursesByMemberId(Long memberId);

	List<CourseEntity> findParticipatingCoursesByMemberId(Long memberId);

	//  === Projection 조회  ===
	List<CourseParticipationInfo> findParticipatingCoursesWithRoleByMemberId(Long memberId);

	Optional<CourseDetailInfo> findCourseBasicDetailsById(Long courseId);

	// === 인가(Authorization) 관련 쿼리 ===

	boolean existsByMemberIdAndCourseIdAndRole(
		@Param("memberId") Long memberId,
		@Param("courseId") Long courseId,
		@Param("role") CourseRole role
	);

	boolean existsByMemberIdAndCourseIdAndRoleIn(
		@Param("memberId") Long memberId,
		@Param("courseId") Long courseId,
		@Param("roles") List<CourseRole> roles
	);

	boolean existsByMemberIdAndCourseId(
		@Param("memberId") Long memberId,
		@Param("courseId") Long courseId
	);
}
