package me.chan99k.learningmanager.adapter.web.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
	@Email
	@NotBlank(message = "이메일 입력은 필수입니다")
	String email,
	@NotBlank(message = "비밀번호 입력은 필수입니다")
	String password
) {
}