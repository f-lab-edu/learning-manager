package me.chan99k.learningmanager.adapter.persistence.session;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.session.requires.SessionQueryRepository;
import me.chan99k.learningmanager.domain.session.Session;

@Repository
public class SessionQueryAdapter implements SessionQueryRepository {
	private final SessionJpaRepository jpaRepository;

	public SessionQueryAdapter(SessionJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Optional<Session> findById(Long sessionId) {
		return jpaRepository.findById(sessionId);
	}

	@Override
	public List<Session> findByCourseId(Long courseId) {
		return jpaRepository.findByCourseId(courseId);
	}

	@Override
	public List<Session> findByCurriculumId(Long curriculumId) {
		return jpaRepository.findByCurriculumId(curriculumId);
	}

	@Override
	public List<Session> findByParentId(Long parentId) {
		return jpaRepository.findByParentId(parentId);
	}

	@Override
	public Optional<Session> findManagedSessionById(Long sessionId, Long memberId) {
		return jpaRepository.findManagedSessionById(sessionId, memberId);
	}
}
