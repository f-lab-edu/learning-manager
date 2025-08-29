package me.chan99k.learningmanager.adapter.auth;

import java.time.Duration;

public interface PasswordResetTokenManager {
	String generateAndStoreToken(String email, Duration expiration);

	boolean validateToken(String token);

	String getEmailByToken(String token);

	void removeToken(String token);
}
