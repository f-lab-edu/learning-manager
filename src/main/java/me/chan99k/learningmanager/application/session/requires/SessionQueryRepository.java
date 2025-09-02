package me.chan99k.learningmanager.application.session.requires;

import java.util.List;
import java.util.Optional;

import me.chan99k.learningmanager.domain.session.Session;

public interface SessionQueryRepository {
	Optional<Session> findById(Long sessionId);

	List<Session> findByCourseId(Long courseId);

	List<Session> findByCurriculumId(Long curriculumId);

	List<Session> findByParentId(Long parentId);

	Optional<Session> findManagedSessionById(Long sessionId, Long memberId);
}
