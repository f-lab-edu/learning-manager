package me.chan99k.learningmanager.authentication;

import jakarta.validation.constraints.NotBlank;

public interface RefreshAccessToken {

	Response refresh(Request request);

	record Request(
		@NotBlank(message = "[AUTH] 리프레시 토큰은 필수입니다.")
		String refreshToken
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
