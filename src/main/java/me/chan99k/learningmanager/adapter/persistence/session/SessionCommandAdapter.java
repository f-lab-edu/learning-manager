package me.chan99k.learningmanager.adapter.persistence.session;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.application.session.requires.SessionCommandRepository;
import me.chan99k.learningmanager.domain.session.Session;

@Repository
public class SessionCommandAdapter implements SessionCommandRepository {
	private final SessionJpaRepository jpaRepository;

	public SessionCommandAdapter(SessionJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Session create(Session session) {
		return jpaRepository.save(session);
	}

	@Override
	public Session save(Session session) {
		return jpaRepository.save(session);
	}

	@Override
	public void delete(Session session) {
		jpaRepository.delete(session);
	}
}
