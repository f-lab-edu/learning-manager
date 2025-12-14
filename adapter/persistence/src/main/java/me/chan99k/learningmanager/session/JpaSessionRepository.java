package me.chan99k.learningmanager.session;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import me.chan99k.learningmanager.session.entity.SessionEntity;

public interface JpaSessionRepository extends JpaRepository<SessionEntity, Long>, CustomSessionRepository {

	List<SessionEntity> findByCourseId(Long courseId);

	List<SessionEntity> findByCurriculumId(Long curriculumId);

	List<SessionEntity> findByParentId(Long parentId);
}
