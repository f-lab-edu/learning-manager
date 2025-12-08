package me.chan99k.learningmanager.session;

import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.session.entity.SessionEntity;
import me.chan99k.learningmanager.session.mapper.SessionMapper;

@Repository
public class SessionCommandAdapter implements SessionCommandRepository {
	private final JpaSessionRepository jpaRepository;

	public SessionCommandAdapter(JpaSessionRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Session create(Session session) {
		SessionEntity entity = SessionMapper.toEntity(session);
		SessionEntity saved = jpaRepository.save(entity);
		return SessionMapper.toDomain(saved);
	}

	@Override
	public Session save(Session session) {
		SessionEntity entity = SessionMapper.toEntity(session);
		SessionEntity saved = jpaRepository.save(entity);
		return SessionMapper.toDomain(saved);
	}

	@Override
	public void delete(Session session) {
		SessionEntity entity = SessionMapper.toEntity(session);
		jpaRepository.delete(entity);
	}
}
