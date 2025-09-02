package me.chan99k.learningmanager.adapter.persistence.session;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.chan99k.learningmanager.domain.session.Session;

public interface SessionJpaRepository extends JpaRepository<Session, Long> {

	List<Session> findByCourseId(Long courseId);

	List<Session> findByCurriculumId(Long curriculumId);

	List<Session> findByParentId(Long parentId);

	@Query("SELECT s FROM Session s " +
		"LEFT JOIN Course c ON s.courseId = c.id " +
		"LEFT JOIN c.courseMemberList cm ON cm.courseRole = 'MANAGER' " +
		"WHERE s.id = :sessionId " +
		"AND (s.courseId IS NULL OR cm.memberId = :memberId)")
	Optional<Session> findManagedSessionById(@Param("sessionId") Long sessionId,
		@Param("memberId") Long memberId);
}
