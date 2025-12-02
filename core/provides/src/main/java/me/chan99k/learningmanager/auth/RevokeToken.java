package me.chan99k.learningmanager.auth;

public interface RevokeToken {

	void revoke(Request request);

	record Request(
		String token,
		String tokenTypeHint   // "refresh_token" 또는 "access_token" (optional)
	) {
		public Request(String token) {
			this(token, null);
		}
	}
}
