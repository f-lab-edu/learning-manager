package me.chan99k.learningmanager.auth;

public interface RefreshAccessToken {

	Response refresh(Request request);

	record Request(
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
