package me.chan99k.learningmanager.session;

public interface SessionCommandRepository {
	Session create(Session session);

	Session save(Session session);

	void delete(Session session);
}
