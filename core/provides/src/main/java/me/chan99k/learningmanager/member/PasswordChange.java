package me.chan99k.learningmanager.member;

import jakarta.validation.constraints.NotBlank;

public interface PasswordChange {
	void changePassword(Long memberId, Request request);

	record Request(
		@NotBlank(message = "[System] 현재 비밀번호는 필수입니다.")
		String currentPassword,

		@NotBlank(message = "[System] 새 비밀번호는 필수입니다.")
		String newPassword
	) {
	}
}
