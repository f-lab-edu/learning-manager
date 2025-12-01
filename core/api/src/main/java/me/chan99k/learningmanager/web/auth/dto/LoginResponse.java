package me.chan99k.learningmanager.web.auth.dto;

public record LoginResponse(
	String accessToken,
	String refreshToken,
	String tokenType,
	Long memberId,
	String email
) {
	public static LoginResponse of(String accessToken, String refreshToken, Long memberId, String email) {
		return new LoginResponse(accessToken, refreshToken, "Bearer", memberId, email);
	}
}