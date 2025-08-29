package me.chan99k.learningmanager.adapter.auth;

import me.chan99k.learningmanager.domain.member.Email;

public interface PasswordResetTokenProvider {
	/**
	 * 이메일로 비밀번호 재설정 토큰 생성
	 */
	String createResetToken(String email);

	boolean validateResetToken(String token);

	Email getEmailFromResetToken(String token);

	/**
	 * 일회용 토큰 사용 후 무효화
	 */
	void invalidateAfterUse(String token);
}
