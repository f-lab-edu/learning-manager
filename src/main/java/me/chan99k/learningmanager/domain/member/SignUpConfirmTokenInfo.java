package me.chan99k.learningmanager.domain.member;

import java.time.Duration;
import java.time.Instant;

/**
 * 회원가입 확인 토큰 정보를 담는 불변 레코드
 */
public record SignUpConfirmTokenInfo(
	Long memberId,           // 회원 ID
	String email,            // 회원 이메일
	Instant createdAt,       // 토큰 생성 시간
	Instant expiresAt,       // 토큰 만료 시간
	boolean used,            // 사용 여부 (일회성 보장)
	String ipAddress,        // 토큰 생성 시 IP (보안 로그용)
	int attemptCount         // 검증 시도 횟수 (브루트포스 방지)
) {

	/**
	 * 새로운 토큰 정보 생성
	 */
	public static SignUpConfirmTokenInfo create(Long memberId, String email,
		Duration expiration, String ipAddress) {
		Instant now = Instant.now();
		return new SignUpConfirmTokenInfo(
			memberId, email, now, now.plus(expiration),
			false, ipAddress, 0
		);
	}

	/**
	 * 만료 여부 확인
	 */
	public boolean isExpired() {
		return Instant.now().isAfter(expiresAt);
	}

	/**
	 * 사용 가능 여부 확인 (사용되지 않았고, 만료되지 않았고, 시도 횟수가 5회 미만)
	 */
	public boolean isUsable() {
		return !used && !isExpired() && attemptCount < 5;
	}

	/**
	 * 시도 횟수 증가한 새로운 인스턴스 반환
	 */
	public SignUpConfirmTokenInfo incrementAttempt() {
		return new SignUpConfirmTokenInfo(
			memberId, email, createdAt, expiresAt,
			used, ipAddress, attemptCount + 1
		);
	}

	/**
	 * 사용 완료 표시한 새로운 인스턴스 반환
	 */
	public SignUpConfirmTokenInfo markAsUsed() {
		return new SignUpConfirmTokenInfo(
			memberId, email, createdAt, expiresAt,
			true, ipAddress, attemptCount
		);
	}
}