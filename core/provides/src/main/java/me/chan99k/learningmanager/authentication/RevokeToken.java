package me.chan99k.learningmanager.authentication;

import jakarta.validation.constraints.NotBlank;

public interface RevokeToken {

	void revoke(Request request);

	record Request(
		@NotBlank(message = "[AUTH] 토큰은 필수입니다.")
		String token,
		String tokenTypeHint   // "refresh_token" 또는 "access_token" (optional)
	) {
		public Request(String token) {
			this(token, null);
		}
	}
}
