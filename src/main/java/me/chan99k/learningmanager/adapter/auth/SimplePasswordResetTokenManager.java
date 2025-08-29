package me.chan99k.learningmanager.adapter.auth;

import java.time.Duration;

import org.springframework.stereotype.Component;

@Component
public class SimplePasswordResetTokenManager extends AbstractTokenManager<String> implements PasswordResetTokenManager {
	@Override
	public String generateAndStoreToken(String email, Duration expiration) {
		return super.generateAndStoreToken(email, expiration);
	}

	@Override
	public boolean validateToken(String token) {
		return super.validateToken(token);
	}

	@Override
	public String getEmailByToken(String token) {
		return super.getDataByToken(token);
	}

	@Override
	public void removeToken(String token) {
		super.removeToken(token);
	}
}
