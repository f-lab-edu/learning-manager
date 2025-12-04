package me.chan99k.learningmanager.auth;

import me.chan99k.learningmanager.member.Email;

public interface PasswordResetTokenProvider {

	String createAndStoreToken(Email email);

	boolean validateResetToken(String token);

	String getEmailFromResetToken(String token);

	/**
	 * 사용 완료된 토큰을 무효화
	 * 비밀번호 변경이 성공적으로 완료된 후 호출
	 *
	 * @param token 무효화할 토큰
	 */
	void invalidateAfterUse(String token);
}
