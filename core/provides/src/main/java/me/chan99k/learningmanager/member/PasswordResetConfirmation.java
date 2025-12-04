package me.chan99k.learningmanager.member;

import jakarta.validation.constraints.NotBlank;

public interface PasswordResetConfirmation {
	void confirmReset(Request request);

	record Request(
		@NotBlank(message = "[System] 토큰은 필수입니다.")
		String token,
		@NotBlank(message = "[System] 새 비밀번호는 필수입니다.")
		String newPassword
	) {
	}
}
