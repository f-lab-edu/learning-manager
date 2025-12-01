package me.chan99k.learningmanager.adapter.auth;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import me.chan99k.learningmanager.domain.member.SignUpConfirmTokenInfo;
import me.chan99k.learningmanager.domain.member.SignUpConfirmer;

@Component
public class OpaqueSignUpConfirmer implements SignUpConfirmer {

	private static final Logger log = LoggerFactory.getLogger(OpaqueSignUpConfirmer.class);

	// 토큰 저장소 (운영환경에서는 Redis로 교체 예정)
	private final ConcurrentHashMap<String, SignUpConfirmTokenInfo> tokenStore = new ConcurrentHashMap<>();

	private final SecureRandom secureRandom = new SecureRandom();

	@Override
	public String generateAndStoreToken(Long memberId, String email, Duration expiration) {
		String token = generateSecureToken();
		String ipAddress = getCurrentIpAddress();

		SignUpConfirmTokenInfo tokenInfo = SignUpConfirmTokenInfo.create(
			memberId, email, expiration, ipAddress
		);

		tokenStore.put(token, tokenInfo);

		log.info("SignUp confirmation token generated for member: {} from IP: {}",
			memberId, ipAddress);

		return token;
	}

	@Override
	public boolean validateToken(String token) {
		if (token == null || token.trim().isEmpty()) {
			return false;
		}

		SignUpConfirmTokenInfo tokenInfo = tokenStore.get(token);
		if (tokenInfo == null) {
			log.warn("SignUp token validation failed: token not found");
			return false;
		}

		// 시도 횟수 증가
		tokenStore.put(token, tokenInfo.incrementAttempt());

		boolean isUsable = tokenInfo.isUsable();
		if (!isUsable) {
			log.warn("SignUp token validation failed: token not usable (expired: {}, used: {}, attempts: {})",
				tokenInfo.isExpired(), tokenInfo.used(), tokenInfo.attemptCount());
		}

		return isUsable;
	}

	@Override
	public Long getMemberIdByToken(String token) {
		SignUpConfirmTokenInfo tokenInfo = tokenStore.get(token);

		if (tokenInfo == null || !tokenInfo.isUsable()) {
			log.error("Attempt to get member ID from invalid signup token");
			throw new IllegalArgumentException("Invalid or expired signup token");
		}

		return tokenInfo.memberId();
	}

	@Override
	public void removeToken(String token) {
		SignUpConfirmTokenInfo tokenInfo = tokenStore.get(token);
		if (tokenInfo != null) {
			// 사용 완료 표시
			tokenStore.put(token, tokenInfo.markAsUsed());
			log.info("SignUp token marked as used for member: {}", tokenInfo.memberId());
		}
	}

	public int getTokenCount() {
		return tokenStore.size();
	}

	public long getExpiredTokenCount() {
		return tokenStore.values().stream()
			.mapToLong(info -> info.isExpired() ? 1 : 0)
			.sum();
	}

	public void cleanupExpiredTokens() {
		tokenStore.entrySet().removeIf(entry -> {
			SignUpConfirmTokenInfo tokenInfo = entry.getValue();
			// 만료된 지 24시간 이상 지난 토큰 제거
			return tokenInfo.isExpired() &&
				tokenInfo.expiresAt().isBefore(java.time.Instant.now().minus(Duration.ofHours(24)));
		});

		log.debug("Cleaned up expired signup tokens. Remaining tokens: {}", tokenStore.size());
	}

	private String generateSecureToken() {
		byte[] randomBytes = new byte[32];
		secureRandom.nextBytes(randomBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
	}

	private String getCurrentIpAddress() {
		try {
			ServletRequestAttributes attributes =
				(ServletRequestAttributes)RequestContextHolder.getRequestAttributes();

			if (attributes != null) {
				HttpServletRequest request = attributes.getRequest();

				String xForwardedFor = request.getHeader("X-Forwarded-For");
				if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
					return xForwardedFor.split(",")[0].trim();
				}

				String xRealIp = request.getHeader("X-Real-IP");
				if (xRealIp != null && !xRealIp.isEmpty()) {
					return xRealIp;
				}

				return request.getRemoteAddr();
			}
		} catch (Exception e) {
			log.warn("Failed to get IP address from request context", e);
		}

		return "unknown";
	}

}