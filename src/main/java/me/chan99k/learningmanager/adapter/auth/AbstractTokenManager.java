package me.chan99k.learningmanager.adapter.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;

// TODO :: 리팩터링 과정 블로그 글로 작성하기
public abstract class AbstractTokenManager<T> {
	private final Map<String, TokenData<T>> tokenStore = new ConcurrentHashMap<>(); // 스레드 안전 보장을 위해

	protected String generateAndStoreToken(T data, Duration expiration) {
		String token = UUID.randomUUID().toString().substring(0, 11);
		Instant expiresAt = Instant.now().plus(expiration);

		var tokenData = new TokenData<>(data, expiresAt);
		tokenStore.put(token, tokenData);

		return token;
	}

	protected boolean validateToken(String token) {
		var data = tokenStore.get(token);

		Instant now = Instant.now();
		if (data != null && now.isBefore(data.expiresAt())) {
			return true;
		}
		if (data != null && now.isAfter(data.expiresAt())) {
			tokenStore.remove(token);
		}

		return false;
	}

	protected T getDataByToken(String token) {
		var tokenData = tokenStore.get(token);
		return tokenData != null ? tokenData.data : null;
	}

	protected void removeToken(String token) {
		tokenStore.remove(token);
	}

	@Scheduled(fixedRate = 600_000) // 10분마다 정리
	private void cleanupExpiredTokens() {
		Instant now = Instant.now();
		tokenStore.entrySet().removeIf(entry ->
			now.isAfter(entry.getValue().expiresAt()));
	}

	protected record TokenData<T>(T data, Instant expiresAt) {
	}
}
