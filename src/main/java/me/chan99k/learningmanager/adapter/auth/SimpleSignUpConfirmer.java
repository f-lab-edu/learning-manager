package me.chan99k.learningmanager.adapter.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.domain.member.SignUpConfirmer;

@Component
public class SimpleSignUpConfirmer implements SignUpConfirmer {
	private final Map<String, ActivationData> tokenStore = new ConcurrentHashMap<>(); // 스레드 안전 보장을 위해

	@Override
	public String generateAndStoreToken(Long memberId, String email, Duration expiration) {
		String token = UUID.randomUUID().toString().substring(0, 11);
		Instant expiresAt = Instant.now().plus(expiration);

		var data = new ActivationData(memberId, email, expiresAt);
		tokenStore.put(token, data);

		return token;
	}

	@Override
	public boolean validateToken(String token) {
		var data = tokenStore.get(token);
		if (data != null && Instant.now().isBefore(data.expiresAt())) {
			return true;
		}
		if (data != null && Instant.now().isAfter(data.expiresAt())) {
			tokenStore.remove(token);
		}

		return false;
	}

	@Override
	public Long getMemberIdByToken(String token) {
		var data = tokenStore.get(token);
		if (data != null && Instant.now().isBefore(data.expiresAt())) {
			return data.memberId();
		}
		if (data != null && Instant.now().isAfter(data.expiresAt())) {
			tokenStore.remove(token);
		}

		return null;
	}

	@Override
	public void removeToken(String token) {
		tokenStore.remove(token);
	}

	@Scheduled(fixedRate = 600000)
	private void cleanupExpiredTokens() {
		Instant now = Instant.now();
		tokenStore.entrySet().removeIf(entry ->
			now.isAfter(entry.getValue().expiresAt()));
	}

	private record ActivationData(Long memberId, String email, Instant expiresAt) {
	}

}
