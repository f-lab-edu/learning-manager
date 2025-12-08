package me.chan99k.learningmanager.authentication;

import jakarta.validation.constraints.NotBlank;

public interface IssueToken {

	Response issueToken(Request request);

	record Request(
		@NotBlank(message = "[AUTH] 이메일은 필수입니다.")
		String email,
		@NotBlank(message = "[AUTH] 비밀번호는 필수입니다.")
		String password
	) {
	}

	record Response(
		String accessToken,
		String refreshToken,
		String tokenType,
		long expiresIn
	) {
		public static Response of(String accessToken, String refreshToken, long expiresInSeconds) {
			return new Response(accessToken, refreshToken, "Bearer", expiresInSeconds);
		}
	}
}
