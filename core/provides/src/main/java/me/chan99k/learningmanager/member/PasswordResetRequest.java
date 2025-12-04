package me.chan99k.learningmanager.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public interface PasswordResetRequest {
	void requestReset(Request request);

	record Request(
		@NotBlank(message = "[System] 이메일은 필수입니다.")
		@Email(message = "[System] 유효한 이메일 형식이 아닙니다.")
		String email
	) {
	}
}
