package me.chan99k.learningmanager.auth;

public interface IssueToken {

	Response issueToken(Request request);

	record Request(
		String email,
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
