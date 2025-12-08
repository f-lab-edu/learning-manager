package me.chan99k.learningmanager.authentication;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import me.chan99k.learningmanager.member.Email;

@Repository
public class InMemoryPasswordResetTokenRepository {

	private final ConcurrentHashMap<String, TokenData> tokenStore = new ConcurrentHashMap<>();
	private final long validityInSeconds;

	public InMemoryPasswordResetTokenRepository(
		@Value("${auth.password-reset-token.validity-in-seconds:1500}") long validityInSeconds
	) {
		this.validityInSeconds = validityInSeconds;
	}

	public void save(String token, Email email) {
		Instant expiresAt = Instant.now().plusSeconds(validityInSeconds);
		tokenStore.put(token, new TokenData(email.address(), expiresAt));
	}

	public Optional<String> findEmailByToken(String token) {
		TokenData data = tokenStore.get(token);
		if (data == null) {
			return Optional.empty();
		}
		if (data.isExpired()) {
			tokenStore.remove(token);  // 만료된 토큰 정리
			return Optional.empty();
		}
		return Optional.of(data.email());
	}

	public boolean existsAndNotExpired(String token) {
		TokenData data = tokenStore.get(token);
		if (data == null) {
			return false;
		}
		if (data.isExpired()) {
			tokenStore.remove(token);  // 만료된 토큰 정리
			return false;
		}
		return true;
	}

	public void delete(String token) {
		tokenStore.remove(token);
	}

	private record TokenData(String email, Instant expiresAt) {
		boolean isExpired() {
			return Instant.now().isAfter(expiresAt);
		}
	}
}