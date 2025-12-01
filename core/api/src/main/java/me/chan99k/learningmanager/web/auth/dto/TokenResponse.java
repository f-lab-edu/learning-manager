package me.chan99k.learningmanager.web.auth.dto;

public record TokenResponse(
	String accessToken,
	String refreshToken,
	String tokenType
) {
	public static TokenResponse of(String accessToken, String refreshToken) {
		return new TokenResponse(accessToken, refreshToken, "Bearer");
	}
}