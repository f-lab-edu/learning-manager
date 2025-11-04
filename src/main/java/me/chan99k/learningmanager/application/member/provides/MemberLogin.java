package me.chan99k.learningmanager.application.member.provides;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * [P0] 사용자 로그인
 */
public interface MemberLogin {
	MemberLogin.Response login(MemberLogin.Request request);

	record Request(
		@Email
		@NotBlank(message = "이메일 입력은 필수입니다")
		String email,
		@NotBlank(message = "비밀번호 입력은 필수입니다")
		String password
	) {
	}

	record Response(
		String accessToken,
		String refreshToken,
		Long memberId,
		String email
	) {
	}
}
