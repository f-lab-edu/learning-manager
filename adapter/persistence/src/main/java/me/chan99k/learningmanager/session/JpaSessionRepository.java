package me.chan99k.learningmanager.session;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.chan99k.learningmanager.session.dto.SessionInfo;
import me.chan99k.learningmanager.session.entity.SessionEntity;

public interface JpaSessionRepository extends JpaRepository<SessionEntity, Long> {

	List<SessionEntity> findByCourseId(Long courseId);

	List<SessionEntity> findByCurriculumId(Long curriculumId);

	List<SessionEntity> findByParentId(Long parentId);

	@Query("SELECT s FROM SessionEntity s " +
		"LEFT JOIN CourseEntity c ON s.courseId = c.id " +
		"LEFT JOIN c.courseMemberList cm ON cm.courseRole = :courseRole " +
		"WHERE s.id = :sessionId " +
		"AND (s.courseId IS NULL OR cm.memberId = :memberId)")
	Optional<SessionEntity> findManagedSessionById(@Param("sessionId") Long sessionId,
		@Param("memberId") Long memberId,
		@Param("courseRole") me.chan99k.learningmanager.course.CourseRole courseRole);

	@Query("SELECT s FROM SessionEntity s WHERE " +
		"(:type IS NULL OR s.type = :type) AND " +
		"(:location IS NULL OR s.location = :location) AND " +
		"(:startDate IS NULL OR s.scheduledAt >= :startDate) AND " +
		"(:endDate IS NULL OR s.scheduledAt <= :endDate)")
	Page<SessionEntity> findAllWithFilters(
		@Param("type") SessionType type,
		@Param("location") SessionLocation location,
		@Param("startDate") Instant startDate,
		@Param("endDate") Instant endDate,
		Pageable pageable
	);

	@Query("SELECT s FROM SessionEntity s WHERE " +
		"s.courseId = :courseId AND " +
		"(:includeChildSessions = true OR s.parent IS NULL) AND " +
		"(:type IS NULL OR s.type = :type) AND " +
		"(:location IS NULL OR s.location = :location) AND " +
		"(:startDate IS NULL OR s.scheduledAt >= :startDate) AND " +
		"(:endDate IS NULL OR s.scheduledAt <= :endDate)")
	Page<SessionEntity> findByCourseIdWithFilters(
		@Param("courseId") Long courseId,
		@Param("type") SessionType type,
		@Param("location") SessionLocation location,
		@Param("startDate") Instant startDate,
		@Param("endDate") Instant endDate,
		@Param("includeChildSessions") Boolean includeChildSessions,
		Pageable pageable
	);

	@Query("SELECT s FROM SessionEntity s WHERE " +
		"s.curriculumId = :curriculumId AND " +
		"(:includeChildSessions = true OR s.parent IS NULL) AND " +
		"(:type IS NULL OR s.type = :type) AND " +
		"(:location IS NULL OR s.location = :location) AND " +
		"(:startDate IS NULL OR s.scheduledAt >= :startDate) AND " +
		"(:endDate IS NULL OR s.scheduledAt <= :endDate)")
	Page<SessionEntity> findByCurriculumIdWithFilters(
		@Param("curriculumId") Long curriculumId,
		@Param("type") SessionType type,
		@Param("location") SessionLocation location,
		@Param("startDate") Instant startDate,
		@Param("endDate") Instant endDate,
		@Param("includeChildSessions") Boolean includeChildSessions,
		Pageable pageable
	);

	@Query("SELECT s FROM SessionEntity s " +
		"JOIN s.participants p " +
		"WHERE p.memberId = :memberId AND " +
		"(:type IS NULL OR s.type = :type) AND " +
		"(:location IS NULL OR s.location = :location) AND " +
		"(:startDate IS NULL OR s.scheduledAt >= :startDate) AND " +
		"(:endDate IS NULL OR s.scheduledAt <= :endDate)")
	Page<SessionEntity> findByMemberIdWithFilters(
		@Param("memberId") Long memberId,
		@Param("type") SessionType type,
		@Param("location") SessionLocation location,
		@Param("startDate") Instant startDate,
		@Param("endDate") Instant endDate,
		Pageable pageable
	);

	@Query("SELECT s FROM SessionEntity s WHERE " +
		"s.scheduledAt >= :startOfMonth AND s.scheduledAt < :startOfNextMonth AND " +
		"(:type IS NULL OR s.type = :type) AND " +
		"(:location IS NULL OR s.location = :location) AND " +
		"(:courseId IS NULL OR s.courseId = :courseId) AND " +
		"(:curriculumId IS NULL OR s.curriculumId = :curriculumId)")
	List<SessionEntity> findByYearMonth(
		@Param("startOfMonth") Instant startOfMonth,
		@Param("startOfNextMonth") Instant startOfNextMonth,
		@Param("type") SessionType type,
		@Param("location") SessionLocation location,
		@Param("courseId") Long courseId,
		@Param("curriculumId") Long curriculumId
	);

	@Query("SELECT s.id FROM SessionEntity s WHERE " +
		"s.scheduledAt > :startDate AND s.scheduledAt < :endDate AND " +
		"(:courseId IS NULL OR s.courseId = :courseId) AND " +
		"(:curriculumId IS NULL OR s.curriculumId = :curriculumId)")
	List<Long> findIdsByPeriodAndFilters(
		@Param("startDate") Instant startDate,
		@Param("endDate") Instant endDate,
		@Param("courseId") Long courseId,
		@Param("curriculumId") Long curriculumId
	);

	@Query("SELECT new me.chan99k.learningmanager.session.dto.SessionInfo(" +
		"s.id, s.title, s.scheduledAt, s.courseId, c.title, s.curriculumId, cur.title) " +
		"FROM SessionEntity s " +
		"LEFT JOIN CourseEntity c ON s.courseId = c.id " +
		"LEFT JOIN CurriculumEntity cur ON s.curriculumId = cur.id " +
		"WHERE s.id IN :sessionIds")
	List<SessionInfo> findSessionInfoProjectionByIds(@Param("sessionIds") List<Long> sessionIds);
}
