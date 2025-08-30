package me.chan99k.learningmanager.adapter.auth;

public interface TokenRevocationProvider<T> {
	void revokeToken(String token);

	boolean isRevoked(String token);

	void cleanup(T validator);
}
