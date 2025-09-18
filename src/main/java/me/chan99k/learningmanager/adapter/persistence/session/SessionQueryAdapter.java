package me.chan99k.learningmanager.adapter.persistence.session;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.session.requires.SessionQueryRepository;
import me.chan99k.learningmanager.domain.session.Session;
import me.chan99k.learningmanager.domain.session.SessionLocation;
import me.chan99k.learningmanager.domain.session.SessionType;

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

	@Override
	public Page<Session> findAllWithFilters(SessionType type, SessionLocation location,
		Instant startDate, Instant endDate, Pageable pageable) {
		return jpaRepository.findAllWithFilters(type, location, startDate, endDate, pageable);
	}

	@Override
	public Page<Session> findByCourseIdWithFilters(Long courseId, SessionType type,
		SessionLocation location, Instant startDate, Instant endDate,
		Boolean includeChildSessions, Pageable pageable) {
		return jpaRepository.findByCourseIdWithFilters(courseId, type, location,
			startDate, endDate, includeChildSessions, pageable);
	}

	@Override
	public Page<Session> findByCurriculumIdWithFilters(Long curriculumId, SessionType type,
		SessionLocation location, Instant startDate, Instant endDate,
		Boolean includeChildSessions, Pageable pageable) {
		return jpaRepository.findByCurriculumIdWithFilters(curriculumId, type, location,
			startDate, endDate, includeChildSessions, pageable);
	}
}
