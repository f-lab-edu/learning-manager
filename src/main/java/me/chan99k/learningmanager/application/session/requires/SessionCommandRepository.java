package me.chan99k.learningmanager.application.session.requires;

import me.chan99k.learningmanager.domain.session.Session;

public interface SessionCommandRepository {
	Session create(Session session);

	Session save(Session session);

	void delete(Session session);
}
