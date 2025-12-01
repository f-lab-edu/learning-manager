package me.chan99k.learningmanager.adapter.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 회원가입 토큰 자동 정리 스케줄러
 */
@Component
public class SignUpTokenCleanupScheduler {

	private static final Logger log = LoggerFactory.getLogger(SignUpTokenCleanupScheduler.class);

	private final OpaqueSignUpConfirmer signUpConfirmer;

	public SignUpTokenCleanupScheduler(OpaqueSignUpConfirmer signUpConfirmer) {
		this.signUpConfirmer = signUpConfirmer;
	}

	/**
	 * 1시간마다 만료된 토큰 정리
	 */
	@Scheduled(fixedRate = 3600000) // 1시간 = 3,600,000ms
	public void cleanupExpiredTokens() {
		try {
			int beforeCount = signUpConfirmer.getTokenCount();
			signUpConfirmer.cleanupExpiredTokens();
			int afterCount = signUpConfirmer.getTokenCount();

			if (beforeCount != afterCount) {
				log.info("Cleaned up {} expired signup tokens. Remaining: {}",
					beforeCount - afterCount, afterCount);
			}
		} catch (Exception e) {
			log.error("Failed to cleanup expired signup tokens", e);
		}
	}

	/**
	 * 24시간마다 토큰 통계 로깅
	 */
	@Scheduled(fixedRate = 86400000) // 24시간 = 86,400,000ms
	public void logTokenStatistics() {
		try {
			int totalTokens = signUpConfirmer.getTokenCount();
			long expiredTokens = signUpConfirmer.getExpiredTokenCount();

			log.info("SignUp Token Statistics - Total: {}, Expired: {}, Active: {}",
				totalTokens, expiredTokens, totalTokens - expiredTokens);
		} catch (Exception e) {
			log.error("Failed to log signup token statistics", e);
		}
	}

	/**
	 * 매주 일요일 자정에 상세 통계 로깅
	 */
	@Scheduled(cron = "0 0 0 * * SUN") // 일요일 00:00:00
	public void logWeeklyStatistics() {
		try {
			int totalTokens = signUpConfirmer.getTokenCount();
			long expiredTokens = signUpConfirmer.getExpiredTokenCount();
			long activeTokens = totalTokens - expiredTokens;

			log.info("Weekly SignUp Token Report:");
			log.info("  - Total tokens in memory: {}", totalTokens);
			log.info("  - Expired tokens: {}", expiredTokens);
			log.info("  - Active tokens: {}", activeTokens);
			log.info("  - Memory usage efficiency: {}%",
				String.format("%.2f", totalTokens > 0 ? (double)activeTokens / totalTokens * 100 : 0.0));

		} catch (Exception e) {
			log.error("Failed to log weekly signup token statistics", e);
		}
	}
}