package me.chan99k.learningmanager.adapter.persistence.session;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionLocation;
import me.chan99k.learningmanager.domain.session.SessionType;

public interface SessionJpaRepository extends JpaRepository<Session, Long> {

	List<Session> findByCourseId(Long courseId);

	List<Session> findByCurriculumId(Long curriculumId);

	List<Session> findByParentId(Long parentId);

	@Query("SELECT s FROM Session s " +
		"LEFT JOIN Course c ON s.courseId = c.id " +
		"LEFT JOIN c.courseMemberList cm ON cm.courseRole = :courseRole " +
		"WHERE s.id = :sessionId " +
		"AND (s.courseId IS NULL OR cm.memberId = :memberId)")
	Optional<Session> findManagedSessionById(@Param("sessionId") Long sessionId,
		@Param("memberId") Long memberId,
		@Param("courseRole") me.chan99k.learningmanager.domain.course.CourseRole courseRole);

	@Query("SELECT s FROM Session s WHERE " +
		"(:type IS NULL OR s.type = :type) AND " +
		"(:location IS NULL OR s.location = :location) AND " +
		"(:startDate IS NULL OR s.scheduledAt >= :startDate) AND " +
		"(:endDate IS NULL OR s.scheduledAt <= :endDate)")
	Page<Session> findAllWithFilters(
		@Param("type") SessionType type,
		@Param("location") SessionLocation location,
		@Param("startDate") Instant startDate,
		@Param("endDate") Instant endDate,
		Pageable pageable
	);

	@Query("SELECT s FROM Session s WHERE " +
		"s.courseId = :courseId AND " +
		"(:includeChildSessions = true OR s.parent IS NULL) AND " +
		"(:type IS NULL OR s.type = :type) AND " +
		"(:location IS NULL OR s.location = :location) AND " +
		"(:startDate IS NULL OR s.scheduledAt >= :startDate) AND " +
		"(:endDate IS NULL OR s.scheduledAt <= :endDate)")
	Page<Session> findByCourseIdWithFilters(
		@Param("courseId") Long courseId,
		@Param("type") SessionType type,
		@Param("location") SessionLocation location,
		@Param("startDate") Instant startDate,
		@Param("endDate") Instant endDate,
		@Param("includeChildSessions") Boolean includeChildSessions,
		Pageable pageable
	);

	@Query("SELECT s FROM Session s WHERE " +
		"s.curriculumId = :curriculumId AND " +
		"(:includeChildSessions = true OR s.parent IS NULL) AND " +
		"(:type IS NULL OR s.type = :type) AND " +
		"(:location IS NULL OR s.location = :location) AND " +
		"(:startDate IS NULL OR s.scheduledAt >= :startDate) AND " +
		"(:endDate IS NULL OR s.scheduledAt <= :endDate)")
	Page<Session> findByCurriculumIdWithFilters(
		@Param("curriculumId") Long curriculumId,
		@Param("type") SessionType type,
		@Param("location") SessionLocation location,
		@Param("startDate") Instant startDate,
		@Param("endDate") Instant endDate,
		@Param("includeChildSessions") Boolean includeChildSessions,
		Pageable pageable
	);

	@Query("SELECT s FROM Session s " +
		"JOIN s.participants p " +
		"WHERE p.memberId = :memberId AND " +
		"(:type IS NULL OR s.type = :type) AND " +
		"(:location IS NULL OR s.location = :location) AND " +
		"(:startDate IS NULL OR s.scheduledAt >= :startDate) AND " +
		"(:endDate IS NULL OR s.scheduledAt <= :endDate)")
	Page<Session> findByMemberIdWithFilters(
		@Param("memberId") Long memberId,
		@Param("type") SessionType type,
		@Param("location") SessionLocation location,
		@Param("startDate") Instant startDate,
		@Param("endDate") Instant endDate,
		Pageable pageable
	);

	@Query("SELECT s FROM Session s WHERE " +
		"s.scheduledAt >= :startOfMonth AND s.scheduledAt < :startOfNextMonth AND " +
		"(:type IS NULL OR s.type = :type) AND " +
		"(:location IS NULL OR s.location = :location) AND " +
		"(:courseId IS NULL OR s.courseId = :courseId) AND " +
		"(:curriculumId IS NULL OR s.curriculumId = :curriculumId)")
	List<Session> findByYearMonth(
		@Param("startOfMonth") Instant startOfMonth,
		@Param("startOfNextMonth") Instant startOfNextMonth,
		@Param("type") SessionType type,
		@Param("location") SessionLocation location,
		@Param("courseId") Long courseId,
		@Param("curriculumId") Long curriculumId
	);
}
